package com.cgnal.opennlp;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

/**
 *
 */
public class TrainModel {
    public static void main(String[] args) {

        try (OutputStream modelOutputStream = new BufferedOutputStream(
                new FileOutputStream(new File("/Users/dgesino/morepersons.bin")));) {

            Config config = ConfigFactory.load();
            String trainingSet = config.getString("trainingset");
            URL trainingSetUrl = TrainModel.class.getResource(trainingSet);

            ObjectStream<String> lineStream = new PlainTextByLineStream(
                    new FileInputStream(new File(trainingSetUrl.toURI().getPath())), "UTF-8");

            ObjectStream<NameSample> sampleStream =
                    new NameSampleDataStream(lineStream);

            TokenNameFinderModel model = NameFinderME.train(
                    "en", "person",  sampleStream,
                    Collections.<String, Object>emptyMap(), 100, 5);

            model.serialize(modelOutputStream);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
