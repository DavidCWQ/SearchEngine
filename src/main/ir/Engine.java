/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;
import java.io.File;

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The inverted index. */
    // Index index = new HashedIndex();
    Index index = new PersistentHashedIndex();

    /** The indexer creating the search index. */
    Indexer indexer;

    /** The HRanker used for HITS rank search. */
    HITSRanker HRanker;

    /** K-gram index */
    KGramIndex kgIndex;

    /** The searcher used to search the index. */
    Searcher searcher;

    /** Spell checker */
    SpellChecker speller;

    /** The engine GUI. */
    SearchGUI gui;

    /** Directories that should be indexed. */
    ArrayList<String> dirNames = new ArrayList<String>();

    /** Lock to prevent simultaneous access to the index. */
    final Object indexLock = new Object();

    /** The patterns matching non-standard words (e-mail addresses, etc.) */
    String patterns_file = null;

    /** The file containing the logo. */
    String pic_file = "";

    /** The file containing the pagerank scores. */
    String rank_file = "";

    /** The file containing the pagerank IDs and links. */
    String link_file = "";

    /** The file containing the pagerank IDs and corresponding docNames. */
    String title_file = "";

    /** For persistent indexes, we might not need to do any indexing. */
    boolean is_indexing = true;

    /** The directory where the resources are located. */
    public static final String RESOURCE_DIR = "src/main/resources/";

    /** The directory where the datasets are located. */
    public static final String DATASET_DIR = "src/main/datasets/";

    /** The directory where rank-titles are located. */
    public static final String TITLE_DIR = "src/main/ir/pagerank/";


    /* ----------------------------------------------- */


    /**  
     *   Constructor. 
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {
        decodeArgs( args );
        indexer = new Indexer( index, kgIndex, patterns_file );
        HRanker = new HITSRanker( link_file, title_file, index );
        searcher = new Searcher( index, kgIndex, HRanker );
        gui = new SearchGUI( this );
        gui.init();
        /* 
         *   Calls the indexer to index the chosen directory structure.
         *   Access to the index is synchronized since we don't want to 
         *   search at the same time we're indexing new files (this might 
         *   corrupt the index).
         */
        if (is_indexing) {
            synchronized ( indexLock ) {
                gui.displayInfoText( "Indexing, please wait..." );
                long startTime = System.currentTimeMillis();
                for (String dirName : dirNames) {
                    File dokDir = new File(dirName);
                    indexer.processFiles(dokDir, is_indexing);
                    indexer.calcEucLengths(is_indexing);
                    /*
                     * MY CODE here (merge scalable persistent hashed index). Otherwise,
                     * Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
                     * */
                    // index.cleanup();
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                gui.displayInfoText( String.format( "Indexing done in %.1f seconds.", elapsedTime/1000.0 ));
                index.cleanup();
            }
        } else {
            gui.displayInfoText( "Index is loaded from disk." );
        }

        /*
         *   Calls the indexer to load pagerank result in resources.
         *   Access to the index is synchronized since we don't want to
         *   search at the same time we're loading pagerank (this might
         *   corrupt the index).
         */
        if (!rank_file.isEmpty() && !title_file.isEmpty()) {
            synchronized ( indexLock ) {
                gui.displayInfoText( "Index is loaded from disk. " + System.lineSeparator() +
                        "Checking Pagerank, please wait..." );
                indexer.getPageRank(rank_file, title_file);
                gui.displayInfoText( "Index is loaded from disk. " + System.lineSeparator() +
                        "Pagerank is loaded from disk." );
            }
        }
    }


    /* ----------------------------------------------- */

    /**
     *   Decodes the command line arguments.
     */
    private void decodeArgs( String[] args ) {
        int i = 0;
        while ( i < args.length ) {
            switch (args[i]) {
                case "-d":
                    i++;
                    if (i < args.length) {
                        dirNames.add(DATASET_DIR + args[i++]);
                    }
                    break;
                case "-p":
                    i++;
                    if (i < args.length) {
                        patterns_file = RESOURCE_DIR + args[i++];
                    }
                    break;
                case "-l":
                    i++;
                    if (i < args.length) {
                        pic_file = RESOURCE_DIR + args[i++];
                    }
                    break;
                case "-r":
                    i++;
                    if (i < args.length) {
                        rank_file = RESOURCE_DIR + args[i++];
                    }
                    break;
                case "-t":
                    i++;
                    if (i < args.length) {
                        title_file = TITLE_DIR + args[i++];
                    }
                    break;
                case "-lk":
                    i++;
                    if (i < args.length) {
                        link_file = TITLE_DIR + args[i++];
                    }
                    break;
                case "-ni":
                    i++;
                    is_indexing = false;
                    break;
                default:
                    System.err.println("Unknown option: " + args[i]);
                    break;
            }
        }
    }


    /* ----------------------------------------------- */


    public static void main( String[] args ) {
        Engine e = new Engine( args );
    }

}
