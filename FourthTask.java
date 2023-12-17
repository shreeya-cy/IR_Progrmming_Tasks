import java.io.IOException;
import java.util.*;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class FourthTask {
    static int docnum=0;
    static FieldType ft=new FieldType();
    private static Set<String> terms = new HashSet<>();
    private static RealVector f1 =null;
    private static RealVector f2 =null;
    private static RealVector f3 =null;
    private static RealVector f4 =null;
    private static RealVector f5 =null;
    private static RealVector f6 =null;
    static Directory index = new RAMDirectory();

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer customAna = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .build();

        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(customAna);
        IndexWriter writer = new IndexWriter(index, config);

        String d1="Today is sunny.";
        String d2="She is a sunny girl.";
        String d3="To be or not to be.";
        String d4="She is in Berlin today.";
        String d5="Sunny Berlin sunny!";
        String d6="Berlin is always exciting!";
        addDoc(writer, d1);
        addDoc(writer, d2);
        addDoc(writer, d3);
        addDoc(writer, d4);
        addDoc(writer, d5);
        addDoc(writer, d6);
        writer.close();

        IndexReader reader = DirectoryReader.open(index);

        addTerms(reader, 0);
        addTerms(reader, 1);
        addTerms(reader, 2);
        addTerms(reader, 3);
        addTerms(reader, 4);
        addTerms(reader, 5);

        f1 = getTermFrequencies(reader, 0);
        f2 = getTermFrequencies(reader, 1);
        f3 = getTermFrequencies(reader, 2);
        f4 = getTermFrequencies(reader, 3);
        f5 = getTermFrequencies(reader, 4);
        f6 = getTermFrequencies(reader, 5);

        double euk = getEukSimilarity(f1, f2);
        double dot = getDotSimilarity(f1, f2);
        double cos = getCosineSimilarity(f1, f2);

        System.out.println("a) 1)");

        System.out.printf("Euclidean distance similarity between Doc1 and Doc2 = %.4f \n",euk);
        System.out.printf("Dot product similarity between Doc1 and Doc2 = %.4f \n",dot);
        System.out.printf("Cosine similarity between Doc1 and Doc2 = %.4f \n",cos);

        RealVector vector = new ArrayRealVector(terms.size());

        System.out.println("\n2)\nquery: to sunny girl. \nsimilarity score: ");
        int i=0;
        String[] q = {"to", "sunny", "girl"};
        for(String term:terms) {
            int w=0;
            for (String s : q) {
                if (term.equals(s)) {
                    w = 1;
                }
            }
            vector.setEntry(i++, w);
        }

        double sim1 = getCosineSimilarity(vector, f1);
        double sim2 = getCosineSimilarity(vector, f2);
        double sim3 = getCosineSimilarity(vector, f3);
        double sim4 = getCosineSimilarity(vector, f4);
        double sim5 = getCosineSimilarity(vector, f5);
        double sim6 = getCosineSimilarity(vector, f6);
        System.out.printf("Doc 1 = %.4f \n",sim1);
        System.out.printf("Doc 2 = %.4f \n",sim2);
        System.out.printf("Doc 3 = %.4f \n",sim3);
        System.out.printf("Doc 4 = %.4f \n",sim4);
        System.out.printf("Doc 5 = %.4f \n",sim5);
        System.out.printf("Doc 6 = %.4f \n",sim6);

        String[] rank1= {d1,d2,d3,d4,d5,d6};
        double[] rank= {sim1,sim2,sim3,sim4,sim5,sim6};
        double[] rank2={sim1,sim2,sim3,sim4,sim5,sim6};
        Arrays.sort(rank2);


        System.out.println("\nranking: ");
        for(int j = rank.length-1,k=1;j>=0;j--) {
            boolean done=false;
            for(int n = 0; !done; n++){
                if(rank[n]==rank2[j]) {
                    System.out.println("Rank "+k+" = "+rank1[n]);
                    rank[n]=Integer.MAX_VALUE;
                    if(j>0) {
                        if(rank2[j]!=rank2[j-1]) {
                            k++;
                        }
                    }
                    done=true;
                }
            }
        }

        System.out.println("****************************************************************************");
        System.out.println("\nb)");

        String text = d1 + d2 + d3 + d4 + d5 + d6;
        TreeMap<String, ArrayList<Double>> tdIdfMatrix = getTF_IDFmatrix(text, customAna);
        Query query = new QueryParser("content", customAna).parse("She is a sunny girl.");
        System.out.println("QUERY: 'She is a sunny girl.'");
        System.out.println("Scoring using VSM -> ");
        tfIdfScoring(query).forEach((k, v) -> System.out.println(k + " : " + v));
        System.out.println("\nScoring using BM25 model -> ");
        bm25Scoring(query).forEach((k, v) -> System.out.println(k + " : " + v));

    }

    // (a) Part
    private static void addDoc(IndexWriter w, String Main) throws IOException {
        Document doc = new Document();

        ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ft.setStored(true);
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorPositions(true);
        ft.setStoreTermVectorPayloads(true);
        ft.setStoreTermVectorOffsets(true);
        doc.add(new Field("Main", Main, ft));
        w.addDocument(doc);
        docnum++;
    }

    public static double getCosineSimilarity(RealVector s1, RealVector s2){
        return (s1.dotProduct(s2)) / (s1.getNorm() * s2.getNorm());
    }

    public static double getDotSimilarity(RealVector s1, RealVector s2){
        return s1.dotProduct(s2);
    }

    public static double getEukSimilarity(RealVector s1, RealVector s2){
        return s1.getDistance(s2);
    }

    // (b) Part
    private static void addDoc(IndexWriter writer, String content, String docID) throws
            IOException { // function to add a document to IndexWriter
        Document doc = new Document();

        FieldType fType =
                new FieldType(); // custom fieldType to make tracking term positions possible
        fType.setStored(true);
        fType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fType.setTokenized(true);
        fType.setStoreTermVectors(true);
        fType.setStoreTermVectorPositions(true);

        doc.add(new StringField("docID", docID, Field.Store.YES));
        doc.add(new Field("content", content, fType));
        writer.addDocument(doc);
    }

    static RealVector getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, "Main");
        double n=reader.getDocCount("Main");
        TermsEnum termsEnum ;
        termsEnum = vector.iterator();
        Map<String, Integer> frequencies = new HashMap<>();
        RealVector realVector = new ArrayRealVector(terms.size());
        BytesRef text;
        ArrayList<Term> v=new ArrayList<>();
        ArrayList<Long> g=new ArrayList<>();
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            Term termInstance = new Term("Main", term);
            frequencies.put(term, freq);
            v.add(termInstance);
            g.add(termsEnum.totalTermFreq());
        }
        int i = 0;
        double idf;
        double tf;
        double tfidf;
        for (String term1 : terms) {
            if(frequencies.containsKey(term1)) {
                Term curTerm = new Term("Main", term1);
                int index=v.indexOf(curTerm);
                Term termInstance=v.get(index);
                tf=g.get(index);
                double docCount = reader.docFreq(termInstance);
                double z=n/docCount;
                idf=Math.log10(z);
                tfidf=tf*idf;
            } else {
                tfidf=0.0;
            }
            realVector.setEntry(i++, tfidf);
        }
        return realVector;
    }


    static void addTerms(IndexReader reader, int docId) throws IOException {
        Terms vector = reader.getTermVector(docId, "Main");
        TermsEnum termsEnum;
        termsEnum = vector.iterator();
        BytesRef text;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            terms.add(term);
        }
    }

    public static TreeMap<Float, String> bm25Scoring(Query query) throws IOException {
        //function to score the documents on the query using the recommended BM25 similarity
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        TreeMap<Float, String> bm25Scores = new TreeMap<>(Collections.reverseOrder());
        Scorer scorer = query.createWeight(searcher, true, 1).scorer(reader.leaves().get(0));
        DocIdSetIterator docIdSetIterator = scorer.iterator();

        while (docIdSetIterator.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
            bm25Scores.put(scorer.score(), reader.document(scorer.docID()).get("content"));
        }
        return bm25Scores;
    }

    public static TreeMap<String, ArrayList<Double>> getTF_IDFmatrix(String text, Analyzer analyzer)
            throws IOException, ParseException {
        //function to generate TF_IDF matrix of the given documents using a custom Analyzer
        //log base 10 used to calculate idf
        TreeMap<String, ArrayList<Double>> tfIdFmatrix = new TreeMap<>();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);

        String[] textSplit = text.split("[.!?]");
        int i = 1;
        for (String tmp : textSplit) {
            addDoc(writer, tmp, "d" + i);
            i++;
        }
        writer.close();

        ArrayList<String> tokens = new ArrayList<>();
        TokenStream stream = analyzer.tokenStream(text, text);
        stream.reset();

        while (stream.incrementToken()) {
            CharTermAttribute attribute = stream.getAttribute(CharTermAttribute.class);
            tokens.add(attribute.toString());
        }
        stream.close();

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        int totalDocs = reader.numDocs();

        Term term;
        PostingsEnum docEnum;
        for (String token : tokens) {
            Query tmpQuery = new QueryParser("content", analyzer).parse(token);
            term = new Term("content", token.toLowerCase());
            docEnum = MultiFields.getTermDocsEnum(reader, "content", term.bytes());
            ArrayList<Double> tfIdfweights = new ArrayList<>(Collections.nCopies(totalDocs, 0.0));
            double termIdf = Math.log10((double) totalDocs / (double) searcher.count(tmpQuery));
            for (int k = 0; k < searcher.count(tmpQuery); k++) {
                docEnum.nextDoc();
                tfIdfweights.add(docEnum.docID(), (double) docEnum.freq() * termIdf);
            }
            tfIdFmatrix.put(token, tfIdfweights);

        }

        return tfIdFmatrix;
    }

    public static TreeMap<Float, String> tfIdfScoring(Query query) throws IOException {
        //function to score the documents on the query using TF_IDF similarity
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new ClassicSimilarity());
        TreeMap<Float, String> tfIdfScores = new TreeMap<>(Collections.reverseOrder());
        Scorer scorer = query.createWeight(searcher, true, 1).scorer(reader.leaves().get(0));
        DocIdSetIterator docIdSetIterator = scorer.iterator();

        while (docIdSetIterator.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
            tfIdfScores.put(scorer.score(), reader.document(scorer.docID()).get("content"));
        }
        return tfIdfScores;
    }

}