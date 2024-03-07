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
    Index index = new HashedIndex();
    // Index index = new PersistentHashedIndex();

    /** The indexer creating the search index. */
    Indexer indexer;

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

    /** The file containing the pageranks. */
    String rank_file = "";

    /** For persistent indexes, we might not need to do any indexing. */
    boolean is_indexing = true;


    /* ----------------------------------------------- */


    /**  
     *   Constructor. 
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {
        decodeArgs( args );
        indexer = new Indexer( index, kgIndex, patterns_file );
        searcher = rank_file.isEmpty()? new Searcher( index, kgIndex ) : null;
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
                    /*
                     * MY CODE here. Otherwise,
                     * Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
                     * */
                    // index.cleanup();
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                gui.displayInfoText( String.format( "Indexing done in %.1f seconds.", elapsedTime/1000.0 ));
                index.cleanup();
            }
        } else {
            gui.displayInfoText( "Index is loaded from disk" );
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
                        dirNames.add(args[i++]);
                    }
                    break;
                case "-p":
                    i++;
                    if (i < args.length) {
                        patterns_file = args[i++];
                    }
                    break;
                case "-l":
                    i++;
                    if (i < args.length) {
                        pic_file = args[i++];
                    }
                    break;
                case "-r":
                    i++;
                    if (i < args.length) {
                        rank_file = args[i++];
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
