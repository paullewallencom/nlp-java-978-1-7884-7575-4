package packt.volume2section2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.out;
import java.nio.charset.Charset;
import java.util.List;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.TrainingParameters;

public class Volume2Section2 {

    private static final String[] SENTENCE = "The cow jumped over the moon.".split(" ");

    public static void main(String[] args) {
        usingOpenNLPPOSModel();

        out.println("Training Example");
        trainOpenNLPPOSModel();
    }

    private static void usingOpenNLPPOSModel() {
        System.out.println("OpenNLP POSModel Examples");
        System.out.println();
        
        try (InputStream modelIn = new FileInputStream(
                new File("en-pos-maxent.bin"));) {
            POSModel posModel = new POSModel(modelIn);
            POSTaggerME posTaggerME = new POSTaggerME(posModel);

            String tags[] = posTaggerME.tag(SENTENCE);

            for (int i = 0; i < SENTENCE.length; i++) {
                System.out.print(SENTENCE[i] + "/" + tags[i] + " ");
            }
            System.out.println();
            System.out.println("\nTop Sequences");
            Sequence topSequences[] = posTaggerME.topKSequences(SENTENCE);
            for (Sequence topSequence : topSequences) {
                System.out.println(topSequence);
            }
            System.out.println();

            System.out.println("Probabilities");
            for (Sequence topSequence : topSequences) {
                List<String> outcomes = topSequence.getOutcomes();
                double[] probabilities = topSequence.getProbs();
                for (int j = 0; j < outcomes.size(); j++) {
                    System.out.printf("%s/%5.3f ", outcomes.get(j), probabilities[j]);
                }
                System.out.println();
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void trainOpenNLPPOSModel() {
        try {
            InputStream inputData = new FileInputStream("sample.train");
            StringBuilder stringBuilder = new StringBuilder();
            while (inputData.available() != 0) {
                stringBuilder.append((char) inputData.read());
            }
            final String trainingText = stringBuilder.toString();

            InputStreamFactory inputStreamfactory
                    = () -> new ByteArrayInputStream(trainingText.getBytes());
            ObjectStream<String> lineStream = new PlainTextByLineStream(
                    inputStreamfactory, Charset.forName("UTF-8"));
            ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

            POSModel model = POSTaggerME.train("en", sampleStream,
                    TrainingParameters.defaultParams(), new POSTaggerFactory());
            POSTaggerME posTaggerME = new POSTaggerME(model);

            String tags[] = posTaggerME.tag(SENTENCE);

            System.out.println();
            for (int i = 0; i < SENTENCE.length; i++) {
                System.out.print(SENTENCE[i] + "_" + tags[i] + " ");
            }
            System.out.println();
            System.out.println();
            System.out.println("Top Sequences");
            Sequence topSequences[] = posTaggerME.topKSequences(SENTENCE);
            for (Sequence topSequence : topSequences) {
                System.out.println(topSequence);
            }
            System.out.println();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
