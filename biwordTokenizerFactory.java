package p03;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class biwordTokenizerFactory extends TokenFilterFactory{


    protected biwordTokenizerFactory(Map args) {
        super(args);
    }

  
    @Override
    public TokenStream create(TokenStream stream) {
        return stream;
    }
}
