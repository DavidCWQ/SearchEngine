/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private final HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList postList = this.getPostings(token);
        // If PostingsList does not exist
        if (postList == null) {
            postList = new PostingsList(docID, offset);
            this.index.put(token, postList);
        }
        // If PostingsList exists
        else {
            postList.insert(docID, offset);
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        // Returns the value to which the specified key is mapped, or
        // defaultValue if this map contains no mapping for the key.
        return index.getOrDefault(token, null);
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
