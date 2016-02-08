package com.cgnal.opennlp;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by dgesino on 15/10/15.
 */
public class NamedEntityRecognizer {

    public static void main(String[] args) throws Exception {

        Config config = ConfigFactory.load();

        String modell = config.getString("organizationModel");

        URL resource = NamedEntityRecognizer.class.getResource(modell);
        InputStream modelIn = new FileInputStream(new File(resource.toURI().getPath()));

        TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
        NameFinderME nameFinder = new NameFinderME(model);

        String[][][] documents = {
                {
                        "Yahoo is a company basd in Palo Alto".split(" "),
                        "Google is a famous company near General Motors".split(" "),
                }
        };

        for (String document[][] : documents) {

            for (String[] sentence : document) {

                Span nameSpans[] = nameFinder.find(sentence);
                System.out.println(Arrays.toString(nameSpans));
            }

            nameFinder.clearAdaptiveData();
        }


    }
}
