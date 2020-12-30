package packt.volume2section3;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.Files;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.out;
import java.util.Random;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class Volume2Section3 {

    public static void main(String[] args) {
        generateTrainingData();
        trainingOpenNLPClassificationModel();
        useOpenNLPClassificationModel();
        
        trainSentimentAnalysisClassifier();
        useSentimentAnalysisClassifier();
    }

    private static void generateTrainingData() {
        out.println("--- Generate Training Data ---");

        try (OutputStream trainingDataInputStream = new FileOutputStream("en-vehicle.train");
                BufferedOutputStream out = new BufferedOutputStream(trainingDataInputStream)) {
            String engines[] = {"", "V8", "V6", "4"};
            String vins[] = {"", "v1203", "v0093", "v11", "v203"};
            String available[] = {"", "na"};

            Random makeRNG = new Random();
            Random engineRNG = new Random();
            Random vinRNG = new Random();
            Random naRNG = new Random();

            for (int i = 0; i < 50000; i++) {
                switch (makeRNG.nextInt(3)) {
                    case 0:
                        out.write(("100 "
                                + available[naRNG.nextInt(available.length)] + " Ford "
                                + available[naRNG.nextInt(available.length)] + " "
                                + engines[engineRNG.nextInt(engines.length)] + " "
                                + vins[vinRNG.nextInt(vins.length)]).getBytes());
                        break;
                    case 1:
                        out.write(("200 "
                                + available[naRNG.nextInt(available.length)] + " Toyota "
                                + available[naRNG.nextInt(available.length)] + " "
                                + engines[engineRNG.nextInt(engines.length)] + " "
                                + vins[vinRNG.nextInt(vins.length)]).getBytes());
                        break;
                    case 2:
                        out.write(("300 "
                                + available[naRNG.nextInt(available.length)] + " Honda "
                                + available[naRNG.nextInt(available.length)] + " "
                                + engines[engineRNG.nextInt(engines.length)] + " "
                                + vins[vinRNG.nextInt(vins.length)]).getBytes());
                        break;
                }
                out.write((engines[engineRNG.nextInt(engines.length)] + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void trainingOpenNLPClassificationModel() {
        out.println("--- Train Model ---");

        try (InputStream trainingDataInputStream = new FileInputStream("en-vehicle.train");
                OutputStream modelOutStream = new FileOutputStream("en-vehicle.model");) {
            ObjectStream<String> lineStream
                    = new PlainTextByLineStream(trainingDataInputStream, "UTF-8");
            ObjectStream<DocumentSample> documentSampleStream = new DocumentSampleStream(lineStream);
            DoccatModel model = DocumentCategorizerME.train("en", documentSampleStream);

            // Save the model
            OutputStream modelOut = new BufferedOutputStream(modelOutStream);
            model.serialize(modelOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void useOpenNLPClassificationModel() {
        out.println("--- Use Model ---");

        try (InputStream modelInputStream = new FileInputStream(
                new File("en-vehicle.model"));) {
            DoccatModel model = new DoccatModel(modelInputStream);
            DocumentCategorizerME categorizer = new DocumentCategorizerME(model);

            double[] outcomes = categorizer.categorize("Toyota v234 V6 corolla ");

            out.println("--- Categories ---");
            for (int i = 0; i < categorizer.getNumberOfCategories(); i++) {
                out.println("Category: " + categorizer.getCategory(i) + " - " + outcomes[i]);
            }
            out.println("\nBest Category: " + categorizer.getBestCategory(outcomes));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Sentiment Analysis Example
    private static final String[] CATEGORIES = {"neg","pos"};
    private static final int NGRAMSIZE = 6;
    private static final DynamicLMClassifier<NGramProcessLM> classifier = 
            DynamicLMClassifier.createNGramProcess(CATEGORIES, NGRAMSIZE);

    private static void trainSentimentAnalysisClassifier() {
        File trainingDirectory = new File("txt_sentoken");
        System.out.println("\nTraining.");
        for (int i = 0; i < CATEGORIES.length; ++i) {
            Classification classification
                    = new Classification(CATEGORIES[i]);
            File file = new File(trainingDirectory, CATEGORIES[i]);
            File[] trainingFiles = file.listFiles();
            for (int j = 0; j < trainingFiles.length; ++j) {
                try {
                    String review = Files.readFromFile(trainingFiles[j], "ISO-8859-1");
                    Classified<CharSequence> classified = 
                            new Classified<>((CharSequence) review, classification);
                    classifier.handle(classified);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void useSentimentAnalysisClassifier() {
        String review = null;
        try (FileReader fr = new FileReader("review.txt");
                BufferedReader br = new BufferedReader(fr);) {
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null) {
                sb.append(line).append(" ");
            }
            review = sb.toString();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Text: " + review);
        Classification classification
                = classifier.classify(review);
        String bestCategory = classification.bestCategory();
        System.out.println("Best Category: " + bestCategory);
    }

}
