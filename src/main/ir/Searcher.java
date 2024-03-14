/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.lang.Math;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;

    /** The ratio to balance between TF-IDF and PageRank scores. */
    double RANK_RATIO = 0.96;
    
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

    /**
     * Performs a ranked search for the given query using the specified
     * ranking and normalization types.
     *
     * @param query       The query to search for.
     * @param rankingType The ranking type to use for scoring.
     * @param normType    The normalization type for the scores.
     * @return A PostingsList containing documents sorted by their ranking scores.
     */
    private PostingsList searchRanked(Query query, RankingType rankingType,
                                      NormalizationType normType) {
        // If the query is an empty one
        if (query.queryTerm.isEmpty()) {
            return null;
        }
        return getRankResult(query, rankingType, normType);
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

    /**
     *  Calculates the ranking scores for documents based on the given query, and
     *  ranking type, and normalization type. TF-IDF type computes vector space
     *  scores by the product of the query vector and doc vectors. (P136/173)
     *  PageRank type computes rank scores by num of outlinks of documents.
     *
     *  @param query is the searching query, containing query terms and weights.
     *  @param normType NUMBER_OF_WORDS(per doc), EUCLIDEAN(magnitude), etc.
     *  @param rankingType is the ranking type to use for scoring and sorting.
     *  @return a posting list containing docIDs sorted by their ranking scores
     *          given query phrase
     */
    private PostingsList getRankResult(Query query, RankingType rankingType,
                                       NormalizationType normType) {
        HashMap<Integer, Double> docScores = new HashMap<>();
        for (Query.QueryTerm queryTerm : query.queryTerm) {
            String term = queryTerm.term;
            // The postings list for term t
            PostingsList postings = index.getPostings(term);
            for (PostingsEntry doc : postings.getList()) {
                // Score = TF-IDF Score * (1 - RANK_RATIO) + PageRank Score * RANK_RATIO
                double score = this.getRankScore(doc, term, queryTerm.weight, rankingType, normType);

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
            double docScore = docScores.get(docID);
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

    /**
     * Calculates the ranking score for a query based on the given ranking type.
     * <p>
     * Reference for COMBINATION: Buntine, Wray L. et al. “Topic-specific scoring of
     * documents for relevant retrieval.” International Conference on Machine Learning
     * (2005).
     *
     * @param doc         The PostingsEntry representing the document.
     * @param term        The query term in query phrase.
     * @param queryWeight The weight of the query term.
     * @param rankingType The ranking type to use for scoring.
     * @param normType    The normalization type for the score.
     *
     * @return The ranking score for the document.
     * @throws IllegalArgumentException If an invalid ranking type is provided.
     */
    private double getRankScore (PostingsEntry doc, String term, double queryWeight,
                                 RankingType rankingType, NormalizationType normType) {
        switch (rankingType) {
            case TF_IDF: {
                double score1 = doc.getLogFreqWeight() * queryWeight * getInvDocFreq(term);
                // TF-IDF res Normalization
                int docLength = index.docLengths.get(doc.docID);
                double eucLength = index.docEucLengths.get(doc.docID);
                return score1 / (normType == NormalizationType.EUCLIDEAN? eucLength : docLength);
            }
            case PAGERANK: {
                return (Math.exp(10 * index.docRanks.get(doc.docID)) - 0.99) * queryWeight;
            }
            case COMBINATION: {
                // Calculate combined score using a weighted combination of TF-IDF and PageRank.
                double score1 = doc.getLogFreqWeight() * queryWeight * getInvDocFreq(term);
                double score2 = (Math.exp(10 * index.docRanks.get(doc.docID)) - 0.99) * queryWeight;
                // TF-IDF res Normalization
                int docLength = index.docLengths.get(doc.docID);
                double eucLength = index.docEucLengths.get(doc.docID);
                score1 /= (normType == NormalizationType.EUCLIDEAN ? eucLength : docLength);
                return score1 * (1 - RANK_RATIO) + score2 * RANK_RATIO;
            }
            case HITS_RANK: {

            }
            default:
                throw new IllegalArgumentException();
        }
    }
}
