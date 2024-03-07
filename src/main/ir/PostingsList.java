/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PostingsList {
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (PostingsEntry entry: this.list) {
            sb.append(entry.toString());
            sb.append("!");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    /** Data Syntax:
     *  docID1: pos1, pos2, ..., post! docID2: pos1, pos2, ..., post! ...
     */
    public static PostingsList toArray(String postingsList) {
        PostingsList pList = null;
        String[] entries = postingsList.split("!");
        ArrayList<String> postingsEntries = new ArrayList<>(Arrays.asList(entries));

        for (String entry : postingsEntries) {
            String[] dataStr = entry.split(":");
            String docID = dataStr[0], posStr = dataStr[1];

            String[] posList = posStr.split(",");
            ArrayList<String> pos = new ArrayList<>(Arrays.asList(posList));

            PostingsEntry postingsEntry = new PostingsEntry(
                    Integer.parseInt(docID),
                    Integer.parseInt(pos.get(0))
            );

            pos.stream().skip(1).forEachOrdered(i -> {
                postingsEntry.positions.add(Integer.parseInt(i));
            });

            if (pList == null)
                pList = new PostingsList(postingsEntry);
            else
                pList.add(postingsEntry);
        }
        return pList;
    }


    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
    return list.get( i );
    }

    /** Returns all the postings. */
    public ArrayList<PostingsEntry> getList() {
        return list;
    }

    /** Insert the entry into the list. */
    public void insert( int docID, int offset ) {
        int pos = this.list.size()-1;
        if (this.list.get(pos).docID != docID) {
            this.list.add(new PostingsEntry(docID, offset));
        }
        else this.list.get(pos).positions.add(offset);
    }

    /** Constructor. */
    public PostingsList( int docID, int offset ) {
        this.list.add(new PostingsEntry(docID, offset));
    }

    public PostingsList( PostingsEntry entry) {
        this.list.add(new PostingsEntry(entry));
    }

    public void add( PostingsEntry entry) {
        this.list.add(new PostingsEntry(entry));
    }

    /** Find intersection of two posting lists (P11/48) */
    public PostingsList intersect ( PostingsList postList2 ) {
        int i = 0, j = 0;
        PostingsList result = null;
        while (i < this.list.size() && j < postList2.size()) {
            int docID1 = this.list.get(i).docID;
            int docID2 = postList2.get(j).docID;
            if (docID1 == docID2) {
                if (result == null)
                    result = new PostingsList(docID1, 0);
                else
                    result.insert(docID1, 0);
                i++; j++;
            }
            else if (docID1 < docID2) { i++; }
            else { j++; }
        }
        return result;
    }

    /** Find places where the two terms appear within next k words of
     *  each other (P42/79)
     *
     *  @return a list giving docID and the term position in p1 and p2
     */
    public PostingsList positionalIntersect( PostingsList postList2, int k ) {
        int i = 0, j = 0;
        PostingsList result = null;

        while (i < this.list.size() && j < postList2.size()) {
            PostingsEntry entry1 = this.list.get(i);
            PostingsEntry entry2 = postList2.get(j);
            int docID1 = entry1.docID;
            int docID2 = entry2.docID;

            if (docID1 == docID2) {
                int pp1 = 0, pp2 = 0;  // postings position ptr.
                ArrayList<Integer> positions1 = entry1.positions;
                ArrayList<Integer> positions2 = entry2.positions;
                ArrayList<Integer> positions = new ArrayList<Integer>();

                while (pp1 < positions1.size() && pp2 < positions2.size()) {
                    int offset = positions2.get(pp2) - positions1.get(pp1);
                    if (0 <= offset && offset <= k) {
                        positions.add(positions2.get(pp2++));
                    }
                    else if (positions2.get(pp2) > positions1.get(pp1)) pp1++;
                    else pp2++;
                }
                // Remove nothing in this case
                // e.g. a big pig is a big pig, pig2 - big1 > k.

                for (int pos : positions) {
                    if (result == null)
                        result = new PostingsList(docID1, pos);
                    else
                        result.insert(docID1, pos);
                }
                i++; j++;
            }
            else if (docID1 < docID2) { i++; }
            else { j++; }
        }
        return result;
    }

    public void sortByScore() {
        this.list.sort(Comparator.comparingDouble(
                PostingsEntry::getScore).reversed());
    }

    //
    //  YOUR CODE HERE
    //
}
