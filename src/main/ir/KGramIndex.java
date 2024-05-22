/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<>();

    /** The directory where the resources are located. */
    static final String RESOURCE_DIR = "src/main/resources/";

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two postings lists
     */
    private List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        // YOUR CODE HERE

        int i = 0, j = 0;
        List<KGramPostingsEntry> intersection = new ArrayList<>();

        if (p1 == null || p2 == null)
            return intersection;

        while (i < p1.size() && j < p2.size()) {
            int id1 = p1.get(i).tokenID;
            int id2 = p2.get(j).tokenID;
            if (id1 == id2) {
                intersection.add(new KGramPostingsEntry(id2));
                i++; j++;
            }
            else if (id1 < id2) { i++; }
            else { j++; }
        }
        return intersection;
    }


    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        // YOUR CODE HERE

        // Return if index already contains the token
        if (getIDByTerm(token) != null) {
            return;
        }

        int termID = generateTermID();
        term2id.put(token, termID);
        id2term.put(termID, token);

        Set<String> kGrams = new HashSet<>();
        String modifiedToken = "^" + token + "$";
        for (int i = 0; i < token.length() + 3 - K; i++) {
            String kGram = modifiedToken.substring(i, i + K);
            if (!kGrams.contains(kGram)) {
                kGrams.add(kGram);
                index.computeIfAbsent(kGram, k -> new ArrayList<>()).add(new KGramPostingsEntry(termID));
            }
        }
    }

    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kGram) {
        return index.getOrDefault(kGram, null);
    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<>();
        int i = 0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", RESOURCE_DIR + args[i++]);
                }
            } else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", RESOURCE_DIR + args[i++]);
                }
            } else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public void searchKGram(String[] kGrams) {
        List<KGramPostingsEntry> postings = null;
        for (String kGram : kGrams) {
            if (kGram.length() != K) {
                System.err.println("Cannot search k-gram index: " + kGram.length() + "-gram provided instead of " + K + "-gram");
                return;
            }

            if (postings == null) {
                postings = getPostings(kGram);
            } else {
                postings = intersect(postings, getPostings(kGram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s).");

            // Print top 50 results
            int showTopResults = 50;
            if (resNum > showTopResults) {
                System.err.println("The first " + showTopResults + " of them are:");
                resNum = showTopResults;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println( (i + 1) + ": " + getTermByID(postings.get(i).tokenID));
            }
        }
    }

    public static void main(String[] arguments) throws IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        String[] kgrams = args.get("kgram").split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 50) {
                System.err.println("The first 50 of them are:");
                resNum = 50;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
    }
}
