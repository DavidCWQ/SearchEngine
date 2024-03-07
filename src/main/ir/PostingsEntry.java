/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public ArrayList<Integer> positions = new ArrayList<Integer>();
    public double score = 0;

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *  <p>
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(docID);
        sb.append(":");
        for (int pos: positions) {
            sb.append(pos);
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    public PostingsEntry( int docID, int offset ) {
        this.docID = docID;
        this.positions.add(offset);
    }

    public PostingsEntry( int docID, double score ) {
        this.docID = docID;
        this.score = score;
    }

    /** Copy Constructor */
    public PostingsEntry( PostingsEntry entry ) {
        this.docID = entry.docID;
        this.positions = new ArrayList<Integer>(entry.positions);
        this.score = entry.score;
    }

    public double getScore() { return this.score; }

    /**
     *  Count the number of occurrences of term in doc.
     *  @return the word count of the term in this doc
     */
    public int getWordCount() {
        return this.positions.size();
    }

    /**
     *  Calculate the log-freq weight of term in doc.
     *  @return the log frequency weight of the term in this doc
     */
    public double getLogFreqWeight() {
        int tf = this.getWordCount();
        return tf > 0 ? Math.log10(tf) + 1D : 0D;
    }

    //
    // YOUR CODE HERE
    //

}

