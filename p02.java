package src;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.BooleanClause;


class p02{
    public static void main(String args[])throws IOException, ParseException{
        Directory myIndex = new RAMDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer(); 
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(myIndex, config);
        String[] docsList = {
            "Today is sunny.",
            "She is a sunny girl.",
            "To be or not to be.",
            "She is in Berlin today.",
            "Sunny Berlin!",
            "Berlin is always exciting!"
        };
        for (int i = 0; i < docsList.length; i++) {
            String text = docsList[i];
            Document doc = new Document();
            doc.add(new TextField( "content", text, Field.Store.YES));
            writer.addDocument(doc);
        } 
        writer.close();
        
        String[] searchTerms = {"sunny", "exciting"};
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        for (String term : searchTerms) {
            QueryParser queryParser = new QueryParser("content", analyzer);
            booleanQueryBuilder.add(queryParser.parse(term), BooleanClause.Occur.MUST);
        }

        BooleanQuery booleanQuery = booleanQueryBuilder.build();
        IndexReader reader = DirectoryReader.open(myIndex);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs results = searcher.search(booleanQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        if(hits.length>0){
        System.out.println("Documents containing 'sunny' and 'exciting':");
        for (ScoreDoc hit : hits) {
            Document hitDoc = searcher.doc(hit.doc);
            System.out.println(hitDoc.get("content"));
        }
        }
        else{ 
            System.out.println("There are no documents containing 'sunny' and 'exciting'.");
        }

        reader.close();
        printPostingList(docsList);
    }


    public static void printPostingList(String[] docsList){
        int[] totalFreq = {0,0};
        int[] docFreq = {0,0};
        String[] targetWord = {"sunny","to"};
        int f1 = 0, f2 = 0, j=0;
        ArrayList<Integer> docID_sunny = new ArrayList<>();
        ArrayList<Integer> freq_sunny = new ArrayList<>();
        ArrayList<Integer> docID_to = new ArrayList<>();
        ArrayList<Integer> freq_to = new ArrayList<>();
        ArrayList<ArrayList<Integer>> pos_sunny = new ArrayList<>();
        ArrayList<ArrayList<Integer>> pos_to = new ArrayList<>();
        for (String doc : docsList){
            String processedDoc = doc.replaceAll("[^a-zA-Z ]", "").toLowerCase();
            String[] words = processedDoc.split(" ");
            ArrayList<Integer> p1 = new ArrayList<>();
            ArrayList<Integer> p2 = new ArrayList<>();
            f1 = 0;
            f2 = 0;
            int temp1 = 0;
            int temp2 = 0;
            for (int i=0;i<words.length;i++){
                if(words[i].equals(targetWord[0])){
                    totalFreq[0]++;
                    if(temp1==0){
                        docID_sunny.add(j+1);
                        docFreq[0]++;
                        p1.add(i+1);
                        f1++;
                    }
                    else{
                    p1.add(i+1);
                    f1++;
                    }
                    temp1++;
                }
                
                if(words[i].equals(targetWord[1])){
                    totalFreq[1]++;
                    if(temp2==0){
                        docID_to.add(j+1);
                        docFreq[1]++;
                        p2.add(i+1);
                        f2++;
                    }
                    else{
                    p2.add(i+1);
                    f2++;
                    }
                    temp2++;
                }
                
            }
            if(f1>0){
                freq_sunny.add(f1);
                pos_sunny.add(p1);
            }
            if(f2>0){
                freq_to.add(f2);
                pos_to.add(p2);
            }
            
            j++;
        }
        System.out.println("Posting list: ");
        System.out.print("[sunny:"+totalFreq[0]+":"+docFreq[0]+"]->");
        int n = 0;
        for(int i=0;i<docFreq[0]-1;i++){
            System.out.print("["+docID_sunny.get(i)+":"+freq_sunny.get(i)+":"+pos_sunny.get(i)+"]->");
        }
        n = docFreq[0]-1;
        System.out.print("["+docID_sunny.get(n)+":"+freq_sunny.get(n)+":"+pos_sunny.get(n)+"]");
        System.out.println();
        System.out.print("[to:"+totalFreq[1]+":"+docFreq[1]+"]->");
        for(int i=0;i<docFreq[1]-1;i++){
            System.out.print("["+docID_to.get(i)+":"+freq_to.get(i)+":"+pos_to.get(i)+"]->");
        }
        n = docFreq[1]-1;
        System.out.print("["+docID_to.get(n)+":"+freq_to.get(n)+":"+pos_to.get(n)+"]");  
    }
}