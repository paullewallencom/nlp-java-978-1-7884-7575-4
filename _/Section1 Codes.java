package packt.volume2section1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.out;
import java.nio.charset.Charset;
import java.util.Random;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

public class Volume2Section1 {

    static final String[] SENTENCES = {"Jack was taller than Peter. ",
            "However, Mr. Smith was taller than both of them. ",
            "The same could be said for Mary and Tommy. ",
            "Mary Anne was the tallest."};

    public static void main(String[] args) {
        out.println("Phone number: 1234567890 Valid: " + isPhoneNumber("1234567890"));
        out.println("Phone number: 123 456 7890 Valid: " + isPhoneNumber("123 456 7890"));
        out.println("Phone number: 123-456-7890 Valid: " + isPhoneNumber("123-456-7890"));
        out.println("Phone number: 123-456-7890 x1234 Valid: " + isPhoneNumber("123-456-7890 x1234"));
        out.println("Phone number: (123)456-7890 Valid: " + isPhoneNumber("(123) 456-7890"));
        out.println();
        out.println();

        openNLPNERExample();

        out.println("---OpenNLP NER Training Example---");
        TokenNameFinderModel tokenNameFinderModel = getTrainingModel(getTrainingText());
        testModel(tokenNameFinderModel);

    }

    private static boolean isPhoneNumber(String phoneNumber) {
//		//Format "1234567890"
////		if (phoneNo.matches("\\d{10}")) return true;
//		if(phoneNumber.matches("[0-9]{10}")) return true;
//		//Format with spaces
//		else if(phoneNumber.matches("\\d{3}\\s\\d{3}\\s\\d{4}")) return true;
//		//Format with dashes
//		else if(phoneNumber.matches("\\d{3}[-]\\d{3}[-]\\d{4}")) return true;
//		//Format with parentheses and dash
//		else if(phoneNumber.matches("\\(\\d{3}\\)\\s\\d{3}-\\d{4}")) return true;
//		//Otherwise return false
//		else return false;

        if (phoneNumber.matches("[0-9]{10}")) {
            return true;
        } else if (phoneNumber.matches("\\d{3}\\s\\d{3}\\s\\d{4}")) {
            return true;
        } else if (phoneNumber.matches("\\d{3}[-]\\d{3}[-]\\d{4}")) {
            return true;
        } else if (phoneNumber.matches("\\(\\d{3}\\)\\s\\d{3}-\\d{4}")) {
            return true;
        } else {
            return false;
        }
    }

    private static void openNLPNERExample() {
        out.println("---OpenNLP NER Example---");
        try (InputStream tokenModelStream = new FileInputStream(
                new File("en-token.bin"));
                InputStream nerModelInputStream = new FileInputStream(
                        new File("en-ner-person.bin"));) {

            TokenizerModel tokenizerModel = new TokenizerModel(tokenModelStream);
            Tokenizer tokenizer = new TokenizerME(tokenizerModel);

            TokenNameFinderModel nameModel
                    = new TokenNameFinderModel(nerModelInputStream);
            NameFinderME nameFinder = new NameFinderME(nameModel);

            for (String sentence : SENTENCES) {
                out.println("Sentence: [" + sentence + "]");
                String tokens[] = tokenizer.tokenize(sentence);
                Span nameSpans[] = nameFinder.find(tokens);
                double[] spanProbabilities = nameFinder.probs(nameSpans);

                for (int i = 0; i < nameSpans.length; i++) {
                    out.println("Span: " + nameSpans[i].toString());
                    if ((nameSpans[i].getEnd() - nameSpans[i].getStart()) == 1) {
                        out.println("Entity: "
                                + tokens[nameSpans[i].getStart()]);
                    } else {
                        out.println("Entity: "
                                + tokens[nameSpans[i].getStart()] + " "
                                + tokens[nameSpans[i].getEnd() - 1]);
                    }
                    out.println("Probability: " + spanProbabilities[i]);
                }
                out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static String getTrainingText() {
        String names[] = {"Bill", "Sue", "Mary Anne", "John Henry", "Patty",
            "Jones", "Smith", "Albertson", "Henry", "Robertson"};
        String prefixes[] = {"", "Mr. ", "", "Dr. ", "", "Mrs. ", "", "Ms. "};
        Random nameIndex = new Random();
        Random prefixIndex = new Random();
        StringBuilder sentences = new StringBuilder();
        for (int i = 0; i < 15000; i++) {
            String line;
            line = "<START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)]
                    + " <END> is <START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)] + " <END> cousin.\n ";

            sentences.append(line);

            line = "Also, <START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)]
                    + " <END> is a parent.\n ";
            sentences.append(line);

            line = "It turns out that <START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)]
                    + " <END> and <START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)] + " <END> are not related.\n ";
            sentences.append(line);

            line = "<START:PERSON> "
                    + prefixes[prefixIndex.nextInt(prefixes.length)]
                    + names[nameIndex.nextInt(names.length)]
                    + " <END> comes from Canada.\n ";
            sentences.append(line);

        }
        return sentences.toString();
    }

    private static TokenNameFinderModel getTrainingModel(final String trainText) {
        InputStreamFactory inputStreamfactory
                = () -> new ByteArrayInputStream(trainText.getBytes());
        try (ObjectStream<String> lineStream = new PlainTextByLineStream(
                inputStreamfactory, Charset.forName("UTF-8"));) {

            ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

            return NameFinderME.train("en", "person", sampleStream,
                    TrainingParameters.defaultParams(), new TokenNameFinderFactory());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void testModel(TokenNameFinderModel tokenNameFinderModel) {
        try (InputStream tokenModelStream = new FileInputStream(
                new File("en-token.bin"));) {

            TokenizerModel tokenizerModel = new TokenizerModel(tokenModelStream);
            Tokenizer tokenizer = new TokenizerME(tokenizerModel);

            NameFinderME nameFinder = new NameFinderME(tokenNameFinderModel);

            for (String sentence : SENTENCES) {
                out.println("Sentence: [" + sentence + "]");
                String tokens[] = tokenizer.tokenize(sentence);
                Span nameSpans[] = nameFinder.find(tokens);
                double[] spanProbabilities = nameFinder.probs(nameSpans);

                for (int i = 0; i < nameSpans.length; i++) {
                    out.println("Span: " + nameSpans[i].toString());
                    if ((nameSpans[i].getEnd() - nameSpans[i].getStart()) == 1) {
                        out.println("Entity: "
                                + tokens[nameSpans[i].getStart()]);
                    } else {
                        out.println("Entity: "
                                + tokens[nameSpans[i].getStart()] + " "
                                + tokens[nameSpans[i].getEnd() - 1]);
                    }
                    out.println("Probability: " + spanProbabilities[i]);
                }
                out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
