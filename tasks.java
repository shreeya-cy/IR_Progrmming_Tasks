package p03;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

public class tasks {
    public static void main(String args[]) throws IOException {
        
        String inputStr = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today. Sunny Berlin! Berlin is always exciting!";
        String query = "New York University";
        String document = " New York has many universities but York University is not one among them.";
        ArrayList<String> queryTokens = new ArrayList<>();
        ArrayList<String> biTokens = new ArrayList<>();
        ArrayList<String> documentTokens = new ArrayList<>();
        StandardTokenizer tmpTokenizer = new StandardTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
        tmpTokenizer.setReader(new StringReader(inputStr.toLowerCase()));
        biwordTokenizer inputStrTokenizer = new biwordTokenizer(tmpTokenizer);
        inputStrTokenizer.reset();
        while(inputStrTokenizer.incrementToken()){
            CharTermAttribute attribute = inputStrTokenizer.getAttribute(CharTermAttribute.class); // to get each token
            biTokens.add(attribute.toString());
        }
        inputStrTokenizer.close();
        System.out.println("Part A Solution:");
        System.out.println(biTokens);

        tmpTokenizer.setReader(new StringReader(query.toLowerCase()));
        biwordTokenizer queryTokenizer = new biwordTokenizer(tmpTokenizer);
        queryTokenizer.reset();
        while(queryTokenizer.incrementToken()){
            CharTermAttribute attribute = queryTokenizer.getAttribute(CharTermAttribute.class); // to get each token
            queryTokens.add(attribute.toString());
        }
        queryTokenizer.close();

        tmpTokenizer.setReader(new StringReader(document.toLowerCase()));
        biwordTokenizer documentTokenizer = new biwordTokenizer(tmpTokenizer);
        documentTokenizer.reset();
        while(documentTokenizer.incrementToken()){
            CharTermAttribute attribute = documentTokenizer.getAttribute(CharTermAttribute.class); // to get each token
            documentTokens.add(attribute.toString());
        }
        documentTokenizer.close();

        System.out.println("Part B Solution:");
        System.out.println("Biword tokens for the given query: ");
        System.out.println(queryTokens);
        System.out.println("False positive example: "+document);
        System.out.println("Biword tokens for the false positive document:");
        System.out.println(documentTokens);
        for(String s:queryTokens){
            if(documentTokens.contains(s)){
                System.out.println("Contains biword in false positive example");
                break;
            }
        }  
}
}