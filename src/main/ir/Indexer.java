/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this Indexer. */
    Index index;

    /** K-gram index to be built up by this Indexer */
    KGramIndex kgIndex;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file;


    /* ----------------------------------------------- */


    /** Constructor */
    public Indexer( Index index, KGramIndex kgIndex, String patterns_file ) {
        this.index = index;
        this.kgIndex = kgIndex;
        this.patterns_file = patterns_file;
    }


    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }


    /**
     *  Tokenizes and indexes the file @code{f}. If <code>f</code> is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processFiles( File f, boolean is_indexing ) {
        // do not try to index fs that cannot be read
        if (is_indexing) {
            if ( f.canRead() ) {
                if ( f.isDirectory() ) {
                    String[] fs = f.list();
                    // an IO error could occur
                    if ( fs != null ) {
                        for (String s : fs) {
                            processFiles(new File(f, s), is_indexing);
                        }
                    }
                } else {
                    // First register the document and get a docID
                    int docID = generateDocID();
                    if ( docID%1000 == 0 ) System.out.println( "Indexed " + docID + " files" );
                    try {
                        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                        Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
                        int offset = 0;
                        while ( tok.hasMoreTokens() ) {
                            String token = tok.nextToken();
                            insertIntoIndex( docID, token, offset++ );
                        }
                        index.docNames.put( docID, f.getPath() );
                        index.docLengths.put( docID, offset );
                        reader.close();
                    } catch ( IOException e ) {
                        System.err.println( "Warning: IOException during indexing." );
                    }
                }
            }
        }
    }

    /** Calculate the Euclidean length of each document vector in TF-IDF space */
    public void calcEucLengths( boolean is_indexing ) {
        if (is_indexing) {
            System.out.println( "Computing EucLength..." );
            for (Integer docID : index.docNames.keySet()) {
                try {
                    String f = index.docNames.get(docID);
                    Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                    Tokenizer tok = new Tokenizer( reader, true, false, true, patterns_file );
                    HashMap<String, Integer> termFreq = new HashMap<String, Integer>();
                    while ( tok.hasMoreTokens() ) {
                        String token = tok.nextToken();
                        termFreq.compute(token, (key, value) -> (value == null) ? 1 : value + 1);
                    }
                    // NEW CODE HERE.
                    double sum = 0.0;
                    for (String term : termFreq.keySet()) {
                        sum += Math.pow(termFreq.get(term) * index.getInvDocFreq(term), 2);
                    }
                    index.docEucLengths.put(docID, Math.pow(sum, 0.5));
                    // NEW CODE ENDS.
                    reader.close();
                } catch ( IOException e ) {
                    System.err.println( "Warning: IOException during EucLength Calculation." );
                }
            }
            System.out.println( "EucLength are saved in docInfo." );
        }
    }


    /* ----------------------------------------------- */


    /**
     *  Indexes one token.
     */
    public void insertIntoIndex( int docID, String token, int offset ) {
        index.insert( token, docID, offset );
        if (kgIndex != null)
            kgIndex.insert(token);
    }

    public void getPageRank(String rankFile, String titleFile) {

        HashMap<Integer, Double> docRank = new HashMap<Integer, Double>();
        HashMap<String, Integer> docName = new HashMap<String, Integer>();

        try (BufferedReader reader = new BufferedReader(new FileReader(rankFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(":");
                if (tokens.length == 2) {
                    int fakeID = Integer.parseInt(tokens[0].trim());
                    double score = Double.parseDouble(tokens[1].trim());
                    docRank.put( fakeID, score );
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(titleFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length == 2) {
                    int fakeID = Integer.parseInt(tokens[0].trim());
                    String title = tokens[1].trim();
                    docName.put( title, fakeID );
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        if (docRank.size() == docName.size()) {
            try {
                // Mapping true ID -> docName -> fake ID -> pagerank score
                for (Integer id : index.docNames.keySet()) {
                    String name = index.docNames.get(id);
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                    Integer f_id = docName.get(name);
                    Double score = docRank.get(f_id);
                    index.docRanks.put( id, score );
                    index.docIDs.put( name, id );
                }
            }
            catch (NullPointerException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
        else {
            throw new InputMismatchException( "Error matching rank file with title file." );
        }

    }
}
