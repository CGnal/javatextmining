package com.cgnal.opennlp;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class NERCustom {

    public static void main(String[] args) throws Exception {

        String sentence = "Gigino went to Paris and he saw the Tour Eiffel, where he met Anton Maria who had a liason with Ginetto and Aurelio.";

        Config config = ConfigFactory.load();

        String enToken = config.getString("enToken");
        URL enTokenUrl = NamedEntityRecognizer.class.getResource(enToken);
        InputStream tokenStream = new FileInputStream(new File(enTokenUrl.toURI().getPath()));
        TokenizerModel tokenModel = new TokenizerModel(tokenStream);
        Tokenizer tokenizer = new TokenizerME(tokenModel);

        //persons model
        String enPerson = config.getString("morePersons");
        URL enPersonUrl = NamedEntityRecognizer.class.getResource(enPerson);
        InputStream modelStream = new FileInputStream(new File(enPersonUrl.toURI().getPath()));
        TokenNameFinderModel entityModel = new TokenNameFinderModel(modelStream);
        NameFinderME personFinder = new NameFinderME(entityModel);


        String tokens[] = tokenizer.tokenize(sentence);
        Span nameSpans[] = personFinder.find(tokens);

        for (int i = 0; i < nameSpans.length; i++) {
            System.out.println("Span: " + nameSpans[i].toString());
            System.out.println("Entity: "
                    + tokens[nameSpans[i].getStart()]);
        }
    }
}
