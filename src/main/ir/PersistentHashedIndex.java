/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "src/main/index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The data file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final int TABLESIZE = 611953;
    static int HASHSIZE = 305947;

    /** The size of an entry */
    public static final int ENTRYSIZE = 12;  // 12-byte = 96-bit

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    // A dictionary that records collisions
    HashMap<String, Integer> cDict = new HashMap<String, Integer>();

    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public static class Entry {
        // YOUR CODE HERE
        public long begin = 0L;
        public int size = 0;

        public Entry(long begin, int size) {
            this.begin = begin;
            this.size = size;
        }
    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
            readCollisions();
        } catch ( FileNotFoundException e ) {
            System.err.println("ERROR: FileNotFound!");
            e.printStackTrace();
        } catch ( IOException e ) {
            System.err.println("ERROR: IO Exception!");
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr ); 
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file.
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        // YOUR CODE HERE
        // Create a ByteBuffer with the required size
        ptr = ptr * ENTRYSIZE;
        ByteBuffer buffer = ByteBuffer.allocate(ENTRYSIZE);

        // Write the entry data to the buffer
        buffer.putLong(entry.begin);
        buffer.putInt(entry.size);

        // Reset the buffer's position to zero
        buffer.flip();

        try {
            dictionaryFile.seek(ptr);
            dictionaryFile.write(buffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr ) {
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        ptr = ptr * ENTRYSIZE;
        byte[] data = new byte[ENTRYSIZE];
        ByteBuffer buffer = ByteBuffer.allocate(ENTRYSIZE);

        try {
            dictionaryFile.seek(ptr);
            dictionaryFile.readFully(data);
            buffer.put(data, 0, data.length);
            buffer.flip();
            return new Entry(buffer.getLong(), buffer.getInt());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for ( Map.Entry<Integer,String> entry : docNames.entrySet() ) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write( docInfoEntry.getBytes() );
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try ( BufferedReader br = new BufferedReader(freader) ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put( Integer.valueOf(data[0]), data[1] );
                docLengths.put( Integer.valueOf(data[0]), Integer.valueOf(data[2]) );
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        // A dictionary that records collision keys
        HashMap<Integer, Long> kDict = new HashMap<Integer, Long>();
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // YOUR CODE HERE
            // Write the dictionary and the postings list
            for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
                String token = entry.getKey();
                int hash = Objects.hash(token) % HASHSIZE + HASHSIZE;  // in case it's negative
                // System.out.print(hash + "\n"); [1, 611952]
                int size = writeData(entry.getValue().toString(), free);

                // Solution: Open Addressing to solve collisions
                // Other solutions include rehashing and separate chaining.
                boolean collide = false;
                while (true) {
                    if (kDict.containsKey(hash)) {
                        if (!collide) {
                            collisions++;
                            collide = true;
                        }
                        hash++;  // Linear probing to detect empty address.
                        if (hash == HASHSIZE - 1) {
                            hash = 1;  // Reset to 1 if it reaches the end.
                        }
                    }
                    else {
                        kDict.put(hash, free);
                        if (collide) {
                            cDict.put(token, hash);
                        }
                        break;
                    }
                }
                writeEntry(new Entry(free, size), hash);
                free += size;
            }
            writeCollisions();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );  // 35614 in this case.
    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        // If the token is already in the cache (index HashMap)
        if (this.index.containsKey(token)) {
            return this.index.get(token);
        }

        int hash = Objects.hash(token) % HASHSIZE + HASHSIZE;
        Entry entry = readEntry(cDict.getOrDefault(token, hash));

        if (entry == null) {
            return null;
        }

        String postingsList;
        postingsList = readData(entry.begin, entry.size);

        PostingsList postings = PostingsList.toArray(postingsList);
        this.index.put(token, postings);

        return postings;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        // YOUR CODE HERE
        PostingsList postList = this.index.getOrDefault(token, null);
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

    public void writeCollisions() {
        try {
            FileOutputStream fileOut = new FileOutputStream(INDEXDIR + "/collisions");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(cDict);
            out.close(); fileOut.close();
            System.out.println("Hash collisions have been saved in collisions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readCollisions() {
        try {
            FileInputStream fileIn = new FileInputStream(INDEXDIR + "/collisions");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            cDict = (HashMap<String, Integer>) in.readObject();
            in.close(); fileIn.close();
            System.out.println("Hash collisions have been read from file.");
        } catch (EOFException e) {
            System.err.println("Hash collisions reaches EOF.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk... " );
        writeIndex();
        System.err.println( "Done! Good Job!" );
    }
}
