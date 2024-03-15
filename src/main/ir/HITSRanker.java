/**
 *   Computes the Hubs and Authorities for an every document in a query-specific
 *   link graph, induced by the base set of pages.
 *
 *   @author Dmytro Kalpakchi
 */

package ir;

import java.util.*;
import java.io.*;


public class HITSRanker {

    /**
     *  Max number of iterations for HITS
     */
    final static int MAX_NUMBER_OF_STEPS = 1000;

    /**
     *  Convergence criterion: hub and authority scores do not
     *  change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.001;

    /**
     *  Factor of linear combination (FACTOR * auth, hub).
     */
    final static double FACTOR = 1.0;

    /**
     *  The inverted index
     */
    Index index;

    /**
     *  Mapping from the titles to internal document ids used in the links file
     */
    HashMap<String,Integer> titleToId = new HashMap<>();

    /**
     *  Mapping from the internal document ids used in the links file to titles
     */
    HashMap<Integer,String> idToTitle = new HashMap<>();

    /**
     *  Sparse vector containing hub scores
     */
    HashMap<Integer,Double> hubs;

    /**
     *  Sparse vector containing authority scores
     */
    HashMap<Integer,Double> authorities;

    /**
     *  A memory-efficient representation of the transition matrix.
     *  The outlinks are represented as a HashMap, whose keys are
     *  the numbers of the documents linked from
     */
    private final HashMap<Integer, HashSet<Integer>> matrix = new HashMap<>();

    /** The directory where the pagerank related files are stored. */
    private static final String RANK_DIR = "src/main/ir/pagerank/";

    /** The directory where the pagerank result are stored. */
    private static final String RESULT_DIR = "src/main/resources/";

    
    /* --------------------------------------------- */

    /**
     * Constructs the HITSRanker object
     * <p>
     * A set of linked documents can be presented as a graph.
     * Each page is a node in graph with a distinct nodeID associated with it.
     * There is an edge between two nodes if there is a link between two pages.
     * <p>
     * Each line in the links file has the following format:
     *  nodeID;outNodeID1,outNodeID2,...,outNodeIDk
     * This means that there are edges between nodeID and outNodeIDi, where i is between 1 and K.
     * <p>
     * Each line in the titles file has the following format:
     *  nodeID;pageTitle
     * <p>
     * NOTE: nodeIDs are consistent between these two files, but they are NOT the same
     *       as docIDs used by search engine's Indexer
     *
     * @param      linksFilename   File containing the links of the graph+
     * @param      titlesFilename  File containing the mapping between nodeIDs and pages titles
     * @param      index           The inverted index
     */
    public HITSRanker( String linksFilename, String titlesFilename, Index index ) {
        this.index = index;
        readDocs( linksFilename, titlesFilename );
    }


    /* --------------------------------------------- */

    /**
     *  A utility function that gets a file name given its path.
     *  For example, given the path "davisWiki/hello.f",
     *  the function will return "hello.f".
     *
     *  @param      path  The file path
     *  @return     The file name.
     */
    private String getFileName( String path ) {
        String result = "";
        StringTokenizer tok = new StringTokenizer( path, File.separator );
        while ( tok.hasMoreTokens() ) {
            result = tok.nextToken();
        }
        return result;
    }


    /**
     *  Reads the files describing the graph of the given set of pages.
     *
     *  @param      linksFilename   File containing the links of the graph
     *  @param      titlesFilename  File containing the mapping between nodeIDs and pages titles
     */
    void readDocs( String linksFilename, String titlesFilename ) {
        // YOUR CODE HERE
        // Read linksFile
        try {
            String line;
            System.err.print( "Reading links file... " );
            BufferedReader in = new BufferedReader( new FileReader( linksFilename ));
            while (( line = in.readLine() ) != null ) {
                int delim = line.indexOf(";");
                Integer fromDoc = Integer.parseInt( line.substring( 0, delim ));
                StringTokenizer tok = new StringTokenizer(
                        line.substring(delim + 1), ","
                );
                HashSet<Integer> outlinks = new HashSet<>();
                while ( tok.hasMoreTokens() ) {
                    String outLink = tok.nextToken();
                    outlinks.add( Integer.parseInt( outLink ));
                }
                this.matrix.put(fromDoc, outlinks);
            }
            System.err.println( "Reading completed." );
        } catch ( FileNotFoundException e ) {
            System.err.println( "Error finding linkFile: " + e.getMessage() );
        } catch ( IOException e ) {
            System.err.println( "Error reading linkFile: " + e.getMessage() );
        }

        // Read titlesFile
        try {
            String line;
            System.err.print( "Reading titles file... " );
            BufferedReader in = new BufferedReader( new FileReader( titlesFilename ));
            while (( line = in.readLine() ) != null ) {
                String[] tokens = line.split(";");
                if ( tokens.length == 2 ) {
                    titleToId.put( tokens[1], Integer.parseInt(tokens[0]) );
                    idToTitle.put( Integer.parseInt(tokens[0]), tokens[1] );
                } else {
                    System.err.println( "Invalid line format: " + line );
                }
            }
            System.err.println( "Reading completed." );
        } catch (FileNotFoundException e) {
            System.err.println( "Error finding titlesFile: " + e.getMessage() );
        } catch (IOException e) {
            System.err.println( "Error reading titlesFile: " + e.getMessage() );
        }

        // Check that the links file matches the titles file
        if (titleToId.size() != matrix.size()) {
            throw new InputMismatchException( "Error matching linksFile with titlesFile." );
        }
    }

    private void updateScores( Integer[] pageIDs, boolean transposeA ) {
        for ( Integer i : pageIDs ) {
            double sum = 0;
            for ( Integer k : pageIDs ) {
                if ( transposeA ) {
                    // If the page has outlinks to a new page
                    if (this.matrix.get(k) != null) {
                        if (this.matrix.get(k).contains(i))
                            sum += hubs.get(k);
                    }
                } else {
                    // If the page has outlinks to a new page
                    if (this.matrix.get(i) != null) {
                        if (this.matrix.get(i).contains(k))
                            sum += authorities.get(k);
                    }
                }
            }
            if ( transposeA ) authorities.replace(i, sum);
            else hubs.replace(i, sum);
        }
    }

    private void normalizeScores( Integer[] pageIDs ) {
        double normA = 0, normH = 0;
        for ( Integer id : pageIDs ) {
            normA += Math.pow( authorities.get(id), 2 );
            normH += Math.pow( hubs.get(id), 2 );
        }
        normA = Math.sqrt(normA);
        normH = Math.sqrt(normH);
        for ( Integer id : pageIDs ) {
            authorities.replace(id, authorities.get(id) / normA);
            hubs.replace(id, hubs.get(id) / normH);
        }
    }

    private boolean isConverge( Double[] prev, Double[] next, boolean print ) {
        double sum = 0.0;
        for ( int j = 0; j < prev.length; j++ ) {
            double sumComponent = prev[j] - next[j];
            sum += sumComponent * sumComponent;
        }
        double res= Math.sqrt(sum);
        if (print) System.out.println( "ε: " + res );
        return res < EPSILON;
    }

    private Double[] getCurrentScores() {
        ArrayList<Double> hubs = new ArrayList<>();
        hubs.addAll( this.hubs.values() );
        hubs.addAll( this.authorities.values() );
        return hubs.toArray( new Double[0] );
    }

    /**
     *  Perform HITS iterations until convergence
     *
     *  @param      titles  The titles of the documents in the root set
     */
    private void iterate( String[] titles, boolean print ) {
        // YOUR CODE HERE
        hubs = new HashMap<>();
        authorities = new HashMap<>();

        HashSet<Integer> baseSet = new HashSet<>();
        for (String title : titles) {
            // Get the rank ID from docTitle
            int f_id = titleToId.get(title);
            // Add the root document
            baseSet.add(f_id);
            // Add documents linking from root document
            if (matrix.get(f_id) != null)
                baseSet.addAll(matrix.get(f_id));
            // Add documents linking to root document
            for (Integer doc : matrix.keySet()) {
                if (matrix.get(doc).contains(f_id)) {
                    baseSet.add(doc);
                }
            }
        }

        Integer[] titleIDs = baseSet.toArray(new Integer[0]);

        // Initialize newA, newH to 1.
        for ( Integer _i : titleIDs ) {
            authorities.put(_i, 1.0);
            hubs.put(_i, 1.0);
        }

        if (print) System.err.println( "Iterating..." );
        for (int i = 0; i < MAX_NUMBER_OF_STEPS; i++) {
            if (print) System.out.print( "Iteration: " + i + ", ");

            Double[] prev = getCurrentScores();
            updateScores( titleIDs, true );
            updateScores( titleIDs, false );
            normalizeScores( titleIDs );
            Double[] next = getCurrentScores();

            // Choose hubs for convergence judgement
            if (isConverge( prev, next, print )) return;
        }
        System.err.println( "Exit iteration." );
    }


    /**
     *  Rank the documents in the subgraph induced by the documents present
     *  in the postings list `post`.
     *
     *  @param      post  The list of postings fulfilling a certain information need
     *  @return     A list of postings ranked according to the hub and authority scores.
     */
    PostingsList rank(PostingsList post) {
        // YOUR CODE HERE
        HashSet<String> titles = new HashSet<>();
        for (PostingsEntry entry: post.getList()) {
            String title = index.docNames.get(entry.docID);
            titles.add(title.substring(title.lastIndexOf(File.separator) + 1));
        }

        iterate(titles.toArray(new String[0]), false);

        PostingsList result = null;
        for (String title : titles) {
            int _i = titleToId.get(title), i = index.docIDs.get(title);
            double docScore = Math.sqrt(authorities.get(_i) * hubs.get(_i));
            // If it is the first intersection
            if (result == null) { result = new PostingsList(new PostingsEntry(i, docScore)); }
            // If it is NOT the first time
            else { result.add(new PostingsEntry(i, docScore)); }
        }

        return result;
    }


    /**
     *  Sort a hash map by values in the descending order
     *
     *  @param      map    A hash map to sorted
     *  @return     A hash map sorted by values
     */
    private HashMap<Integer,Double> sortHashMapByValue(HashMap<Integer,Double> map) {
        if (map == null) {
            return null;
        } else {
            List<Map.Entry<Integer,Double> > list = new ArrayList<>(map.entrySet());
      
            Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
              
            HashMap<Integer,Double> res = new LinkedHashMap<>();
            for (Map.Entry<Integer,Double> el : list) { 
                res.put(el.getKey(), el.getValue()); 
            }
            return res;
        }
    } 


    /**
     *  Write the first `k` entries of a hash map `map` to the file `fName`.
     *
     *  @param      map        A hash map
     *  @param      fName      The filename
     *  @param      k          A number of entries to write
     */
    void writeToFile(HashMap<Integer,Double> map, String fName, int k) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(RESULT_DIR + fName));
            
            if (map != null) {
                int i = 0;
                for (Map.Entry<Integer,Double> e : map.entrySet()) {
                    i++;
                    writer.write(e.getKey() + ": " + String.format("%.5g%n", e.getValue()));
                    if (i >= k) break;
                }
            }
            writer.close();
            System.err.println( "Result saved: " + fName );
        } catch (IOException e) {
            System.err.println( "Error writing results: " + e.getMessage() );
        }
    }


    /**
     *  Rank all the documents in the links file. Produces two files:
     *  hubs_top_30.txt with documents containing top 30 hub scores
     *  authorities_top_30.txt with documents containing top 30 authority scores
     */
    void rank() {
        iterate(titleToId.keySet().toArray(new String[0]), true);
        HashMap<Integer,Double> sortedHubs = sortHashMapByValue(hubs);
        HashMap<Integer,Double> sortedAuthorities = sortHashMapByValue(authorities);
        writeToFile(sortedHubs, "hubs_top_30_result.txt", 30);
        writeToFile(sortedAuthorities, "authorities_top_30_result.txt", 30);
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
        if ( args.length != 2 ) {
            System.err.println( "Please give the names of the link and title files" );
        }
        else {
            HITSRanker hr = new HITSRanker(
                    RANK_DIR + args[0],
                    RANK_DIR + args[1],
                    null
            );
            hr.rank();
        }
    }
}