package org.elasticsearch.index.analysis;

import java.net.URI;

import java.io.File;
import java.io.FileInputStream;

import eu.hlavki.text.lemmagen.api.Lemmatizer;
import eu.hlavki.text.lemmagen.LemmatizerFactory;

import org.apache.lucene.analysis.TokenStream;

import org.elasticsearch.env.Environment;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;

public class LemmagenFilterFactory extends AbstractTokenFilterFactory {
  private Lemmatizer lemmatizer;
  static final String DEFAULT_DIRECTORY = "lemmagen";

  public LemmagenFilterFactory(Environment env, String name, Settings settings) {
    super(name);
    String lexicon = settings.get("lexicon", (String)null);
    String lexiconPath = settings.get("lexicon_path", (String)null);
    if (lexicon == null && lexiconPath == null) {
      throw new IllegalArgumentException("You need to specify lexicon or lexicon_path option in the token filter configuration");
    } else if (lexicon != null && lexiconPath != null) {
      throw new IllegalArgumentException("Both lexicon and lexicon_path can't be specified");
    } else {
      if (lexicon != null) {
        this.lemmatizer = this.getLemmatizer(lexicon, env);
      }

      if (lexiconPath != null) {
        this.lemmatizer = this.getLemmatizer(env.configDir().resolve(lexiconPath).toUri());
      }

    }
  }

  public Lemmatizer getLemmatizer(String lexicon, Environment env) {
    return this.getLemmatizer(env.configDir().resolve(this.getLexiconDefaultPath(lexicon)).toUri());
  }

  public Lemmatizer getLemmatizer(URI lexiconPath) {
    try {
      File lexiconFile = new File(lexiconPath);
      return LemmatizerFactory.read(new FileInputStream(lexiconFile));
    } catch (Exception e) {
      throw new IllegalArgumentException("Can't initialize lemmatizer from resource path " + lexiconPath.toString(), e);
    }
  }

  public TokenStream create(TokenStream tokenStream) {
    return new LemmagenFilter(tokenStream, this.lemmatizer);
  }

  private String getLexiconDefaultPath(String lexicon) {
    return lexicon.endsWith(".lem") ? "lemmagen/" + lexicon : "lemmagen/" + lexicon + ".lem";
  }
}
