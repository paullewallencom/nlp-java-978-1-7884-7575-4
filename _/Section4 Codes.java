package packt.volume2section4;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import static java.lang.System.out;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Volume2Section4 {

    public static void main(String[] args) {
//        usingStanfordLexicalizedParser();
        usingStanfordCoreferenceResolution();
    }

    private static void usingStanfordLexicalizedParser() {
        // https://wiki.csc.calpoly.edu/CSC-581-S11-06/browser/trunk/Stanford/stanford-parser-2011-04-20/englishPCFG.ser.gz?rev=2
        out.println("---Using the Stanford Parser---");
        LexicalizedParser lexicalizedParser = 
                LexicalizedParser.loadModel("englishPCFG.ser.gz");
        String[] senetence = {"He", "went", "to", "the", 
            "store", "to", "buy", "milk", "."};
        List<CoreLabel> coreLabelList = Sentence.toCoreLabelList(senetence);

        Tree parseTree = lexicalizedParser.apply(coreLabelList);
        parseTree.pennPrint();
        System.out.println();

        // You can also use a TreePrint object to print trees and dependencies
        System.out.println("---Using TreePrint Class---");
        TreePrint treePrint = new TreePrint("penn,typedDependenciesCollapsed");
        treePrint.printTree(parseTree);
        
        System.out.println("---TreePrint Format List---");
        for (String format : TreePrint.outputTreeFormats) {
            System.out.println(format);
        }
        System.out.println();
    }
    
    
    private static void usingStanfordCoreferenceResolution() {
        
        out.println("---StanfordCoreferenceResolution---");
        String sentence = "Daniel went to town taking Sarah's donkey with them " 
                + "and he and she got there on time.";
        Properties properties = new Properties();
        properties.put("annotators", 
                "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);
        out.println();
        out.println("Sentence: " + sentence + "\n");
        Map<Integer, CorefChain> corefChainMap = 
                annotation.get(CorefChainAnnotation.class);

        Set<Integer> set = corefChainMap.keySet();
        Iterator<Integer> setIterator = set.iterator();
        while(setIterator.hasNext()) {
            CorefChain corefChain = corefChainMap.get(setIterator.next());
            out.println("CorefChain: " + corefChain);
            out.print("ClusterId: " + corefChain.getChainID());
            CorefMention mention = corefChain.getRepresentativeMention();
            out.println(" CorefMention: " + mention + " Span: [" + 
                    mention.mentionSpan + "]");

            List<CorefMention> mentionList = corefChain.getMentionsInTextualOrder();
            Iterator<CorefMention> mentionIterator = mentionList.iterator();
            while(mentionIterator.hasNext()) {
                CorefMention corefMention = mentionIterator.next();
                out.println("\tMention: " + corefMention + " Span: [" + 
                        mention.mentionSpan + "]");
                out.print("\tMention Type: " + corefMention.mentionType + 
                        " Gender: " + corefMention.gender);
                out.println(" Start: " + corefMention.startIndex + 
                        " End: " + corefMention.endIndex);
            }
            out.println();
        }
    }

}
