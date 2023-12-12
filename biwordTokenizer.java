package p03;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class biwordTokenizer extends TokenFilter{
    private CharTermAttribute charTermAttr;
    int count = 0;
    String word1 = "";

    protected biwordTokenizer(TokenStream input) {
        super(input);
        this.charTermAttr = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
          }
        else{
            if(count == 0){
                word1 = charTermAttr.toString();
                count++;
                input.incrementToken();
            }
        }
        String word2 = charTermAttr.toString();
        String biword = word1.concat(" ".concat(word2));
        word1 = word2;
        charTermAttr.copyBuffer(biword.toCharArray(), 0, biword.toCharArray().length);
        return true;    
        
    }   

}