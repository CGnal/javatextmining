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
public class NER {

    public static void main(String[] args) throws Exception {

        String sentence = "He was the last person to see Fred. He wa working for Yahoo.";

        String sentences[] = {"Joe was the last person to see Fred. ",
                "He saw him in Boston at McKenzie's pub at 3:00 where he "
                        + " paid $2.45 for an ale. ",
                "Robert Marley used to play bongs in Jamaica ",
                "Joe wanted to go to Vermont for the day to visit a cousin who "
                        + "works at IBM, but Sally and he had to look for Fred"};

        Config config = ConfigFactory.load();

        String enToken = config.getString("enToken");
        URL enTokenUrl = NamedEntityRecognizer.class.getResource(enToken);
        InputStream tokenStream = new FileInputStream(new File(enTokenUrl.toURI().getPath()));
        TokenizerModel tokenModel = new TokenizerModel(tokenStream);
        Tokenizer tokenizer = new TokenizerME(tokenModel);

        //persons model
        String enPerson = config.getString("enNerPerson");
        URL enPersonUrl = NamedEntityRecognizer.class.getResource(enPerson);
        InputStream modelStream = new FileInputStream(new File(enPersonUrl.toURI().getPath()));
        TokenNameFinderModel entityModel = new TokenNameFinderModel(modelStream);
        NameFinderME personFinder = new NameFinderME(entityModel);

        //organization model
        String enOrganization = config.getString("organizationModel");
        URL enOrganizationUrl = NamedEntityRecognizer.class.getResource(enOrganization);
        InputStream organizationModelStream = new FileInputStream(new File(enOrganizationUrl.toURI().getPath()));
        TokenNameFinderModel organizationModel = new TokenNameFinderModel(organizationModelStream);
        NameFinderME organizationFinder = new NameFinderME(organizationModel);

        String tokens[] = tokenizer.tokenize(sentence);
        Span nameSpans[] = personFinder.find(tokens);
        Span nameSpansOraganization[] = organizationFinder.find(tokens);

        for (int i = 0; i < nameSpans.length; i++) {
            System.out.println("Span: " + nameSpans[i].toString());
            System.out.println("Entity: "
                    + tokens[nameSpans[i].getStart()]);
        }

        for (int i = 0; i < nameSpansOraganization.length; i++) {
            System.out.println("Span: " + nameSpansOraganization[i].toString());
            System.out.println("Entity: "
                    + tokens[nameSpansOraganization[i].getStart()]);
        }

        for (String s : sentences) {
            String tokens2[] = tokenizer.tokenize(s);
            Span nameSpans2[] = personFinder.find(tokens2);
            for (int i = 0; i < nameSpans2.length; i++) {

                System.out.println("Span: " + nameSpans2[i].toString());
                System.out.println("Entity: "
                        + tokens2[nameSpans2[i].getStart()]);
                double[] spanProbs = personFinder.probs(nameSpans2);
                System.out.println("Probability: " + spanProbs[i]);
            }
            System.out.println();
        }


    }
}
