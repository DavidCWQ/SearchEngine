/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType,
                                NormalizationType normType ) {
        switch (queryType) {
            case INTERSECTION_QUERY:
                return searchIntersection(query);
            case PHRASE_QUERY:
                return searchPhrase(query);
            case RANKED_QUERY:
                return searchRanked(query, rankingType, normType);
            default: throw new IllegalArgumentException();
        }
    }

    private PostingsList searchIntersection(Query query) {
        PostingsList result = null;
        for (Query.QueryTerm term : query.queryTerm) {
            PostingsList postings = index.getPostings(term.term);
            // If no PostingsList contains the query term
            if (postings == null) {
                return null;
                // return new PostingsList();
            }
            // If it is the first intersection
            if (result == null) {
                result = postings;
            }
            // If it is NOT the first time
            else {
                result = result.intersect(postings);
                if (result == null) return null;  // Avoid searching after failure.
            }
        }
        return result;
    }

    private PostingsList searchPhrase(Query query) {
        PostingsList result = null;
        int offset = 1;  // where the two terms appear within next 1 word of each other
        for (Query.QueryTerm term : query.queryTerm) {
            PostingsList postings = index.getPostings(term.term);
            // If one of the queried terms is NOT in the doc
            if (postings == null) {
                return null;
                // return new PostingsList();
            }
            // If it is the first intersection
            if (result == null) {
                result = postings;
            }
            // If it is NOT the first time
            else {
                result = result.positionalIntersect(postings, offset);
                if (result == null) return null;  // Avoid searching after failure.
            }
        }
        return result;
    }

    private PostingsList searchRanked(Query query, RankingType rankingType,
                                      NormalizationType normType) {
        // If the query is an empty one
        if (query.queryTerm.isEmpty()) {
            return null;
        }
        switch (rankingType) {
            case TF_IDF:
                return cosineScore(query, normType);
            case PAGERANK:
                return pageRank(query);
            case COMBINATION:
                return comboRank(query);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     *  Calculate Inverse Document Frequency.
     *  @param term the given term t
     *  @return idf = log(N/df)
     */
    private double getInvDocFreq( String term ) {
        double n = index.docLengths.size();
        double df = index.getPostings(term).size();
        return Math.log10(n/df);
    }

    /** Compute vector space scores by the product of the query vector
     *  and doc vectors. (P136/173)
     *  @param query is the searching query
     *  @param normType NUMBER_OF_WORDS(per doc), EUCLIDEAN(magnitude)
     *  @return a list giving docID and the cos score with query phrase
     */
    private PostingsList cosineScore(Query query, NormalizationType normType) {
        // Create a hash map to store the scores of all documents.
        HashMap<Integer, Double> docScores = new HashMap<>();
        for (Query.QueryTerm queryTerm : query.queryTerm) {
            String term = queryTerm.term;
            // The postings list for term t
            PostingsList postings = index.getPostings(term);
            for (PostingsEntry doc : postings.getList()) {
                // score = tf * idf
                double score = doc.getLogFreqWeight() * queryTerm.weight * getInvDocFreq(term);

                // If the doc already exists in docScores array, add the SCORE to it.
                int docID = doc.docID;
                if (docScores.containsKey(docID)) {
                    docScores.replace(docID, docScores.get(docID) + score);
                }
                // Initialize the doc with score otherwise.
                else {
                    docScores.put(docID, score);
                }
            }
        }

        PostingsList result = null;
        for (Integer docID : docScores.keySet()) {
            // Normalization
            int docLength = index.docLengths.get(docID);
            double docScore = docScores.get(docID) / docLength;

            // If it is the first intersection
            if (result == null) { result = new PostingsList(new PostingsEntry(docID, docScore)); }
            // If it is NOT the first time
            else { result.add(new PostingsEntry(docID, docScore)); }
        }

        // Sort the docs by Score
        if (result == null) return null;
        else result.sortByScore();
        return result;
    }

    private PostingsList pageRank(Query query) {
        return null;
    }

    private PostingsList comboRank(Query query) {
        return null;
    }

}
