/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.nio.charset.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    static class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryTerm = new ArrayList<>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryTerm.add(new QueryTerm(tok.nextToken(), 1.0));
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryTerm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryTerm) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryTerm) {
            queryCopy.queryTerm.add(new QueryTerm(t.term, t.weight));
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        // YOUR CODE HERE

        // Step 0: Initialization
        int numOfRelevantDoc = 0;
        // Map query terms to their occurrences in relevant docs
        Map<String, Integer> newQueryTerms = new HashMap<>();
        // Map query terms to their UPDATED term weights
        Map<String, Double> newQuery = new HashMap<>();

        // Step 1: Extract terms from relevant documents
        for (int i = 0; i < docIsRelevant.length; i++) {
            // Filter out irrelevant documents
            if (docIsRelevant[i]) {
                // Add relevant docs
                numOfRelevantDoc++;
                // Get the relevant document "i" in the results list
                Integer docID = results.get(i).docID;
                // Get the document name according to the docID
                String docName = engine.index.docNames.get(docID);
                // Get terms and their frequencies
                Map<String, Integer> docTerms = getTermFrequencies(docName, engine.patterns_file);
                // Update frequencies in newQueryTerms
                for (Map.Entry<String, Integer> term : docTerms.entrySet()) {
                    newQueryTerms.put(
                            term.getKey(),
                            newQueryTerms.getOrDefault(term.getKey(), 0) + term.getValue()
                    );
                }
            }
        }

        // No relevant documents to provide feedback
        if (numOfRelevantDoc == 0) {
            return;
        }

        // Step 2: Normalize the term frequencies to get term weights
        for (Map.Entry<String, Integer> termEntry : newQueryTerms.entrySet()) {
            newQuery.put(termEntry.getKey(), beta * termEntry.getValue() / (double) numOfRelevantDoc);
        }

        // Step 3: Formulate a new query by integrating with the old query
        for (QueryTerm query : queryTerm) {
            newQuery.put(query.term, newQuery.getOrDefault(query.term, 0.0) + alpha * query.weight);
        }

        // Step 4: Query the engine with the new query
        //  converting the key-value pairs from the newQueryVector map into a list of QueryTerm objects
        queryTerm = (ArrayList<QueryTerm>) newQuery.entrySet().stream()
                .map(q -> new QueryTerm(q.getKey(), q.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Integer> getTermFrequencies(String docName, String patterns_file) {
        HashMap<String, Integer> termFreq = new HashMap<String, Integer>();
        try {
            Reader reader = new InputStreamReader( new FileInputStream(docName), StandardCharsets.UTF_8 );
            Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
            while ( tok.hasMoreTokens() ) {
                String token = tok.nextToken();
                termFreq.compute(token, (key, value) -> (value == null) ? 1 : value + 1);
            }
            reader.close();
        } catch ( IOException e ) {
            System.err.println( "Warning: IOException in relevanceFeedback method." );
        }
        return termFreq;
    }
}
