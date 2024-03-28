/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;

/**
 *  Defines some common data structures and methods that all types of
 *  index should implement.
 */
public interface Index {

    /** Mapping from document identifiers to document names. */
    public HashMap<Integer,String> docNames = new HashMap<Integer,String>();
    
    /** Mapping from document identifier to document Manhattan length. */
    public HashMap<Integer,Integer> docLengths = new HashMap<Integer,Integer>();

    /** Mapping from document identifier to document Euclidean length. */
    public HashMap<Integer,Double> docEucLengths = new HashMap<Integer,Double>();

    /** Mapping from document identifiers to pagerank scores. */
    public HashMap<Integer,Double> docRanks = new HashMap<Integer,Double>();

    /** Mapping from document titles to document identifiers. */
    public HashMap<String,Integer> docIDs = new HashMap<String,Integer>();

    /** Inserts a token into the index. */
    public void insert( String token, int docID, int offset );

    /** Returns the postings for a given term. */
    public PostingsList getPostings( String token );

    /**
     *  Calculate Inverse Document Frequency.
     *  @param term the given term t
     *  @return idf = log(N/df)
     */
    default double getInvDocFreq( String term ) {
        double n = docLengths.size();
        double df = getPostings(term).getList().size();
        return Math.log10(n/df);
    }

    /** This method is called on exit. */
    public void cleanup();

}
