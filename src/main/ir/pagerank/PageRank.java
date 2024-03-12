package ir.pagerank;

import java.util.*;
import java.io.*;
import java.lang.Math;

public class PageRank {

    /**
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names.
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /** The number of outlinks from each node. */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

	/** Max number of iterations or rounds of random walk. */
	int maxIterations;

	/** Create and initialize a thread-safe random number generator. */
	final private Random random = new Random();

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

	/** The directory where the pagerank related files are stored. */
	public static final String PAGERANK_DIR = "src/main/ir/pagerank/";

	/** The directory where the pagerank result are stored. */
	public static final String RESULT_DIR = "src/main/resources/";
    /* --------------------------------------------- */


    public PageRank( String filename, Integer method_id, Integer maxEpochs ) {
		int noOfDocs = readDocs( PAGERANK_DIR + filename );
		maxIterations = noOfDocs * maxEpochs;
		switch (method_id) {
			case 1:
				MonteCarloSim1( noOfDocs );
				break;
			case 2:
			case 4:
			case 5:
				break;
			default:
				iterate( noOfDocs, maxIterations );
				break;
		}
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromDoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromDoc == null ) {
		    // This is a previously unseen doc, so add it to the table.
		    fromDoc = fileIndex++;
		    docNumber.put( title, fromDoc );
		    docName[fromDoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previously unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromDoc to otherDoc.
            link.computeIfAbsent(fromDoc, k -> new HashMap<Integer, Boolean>());
		    if ( link.get(fromDoc).get(otherDoc) == null ) {
			link.get(fromDoc).put( otherDoc, true );
			out[fromDoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /**
     *  Chooses a probability vector a, and repeatedly computes
     *  aP, aP^2, aP^3... until aP^i = aP^(i+1).
	 *  Save the result as txt file named "pagerank_result.txt"
     */
    private void iterate( int numberOfDocs, int maxIterations ) {
		// YOUR CODE HERE

		// Initialize vector a with length=1 and equal probabilities for all docs
		double[] a = new double[numberOfDocs];
		Arrays.fill(a, 1.0 / numberOfDocs);

		// Perform a fixed number of iterations
		System.out.println("Start Power Iteration Calculation: ");
		System.out.println("======================================");
		for (int i = 0; i < maxIterations; i++) {
			// Compute the matrix-vector multiplication aP
			double[] aP = matrixVectorMul(a);
			// Compute the exit condition
			double length = this.distance(a, aP);
			// Exit the loop if |a-aP|<ε0 is achieved
			if (length < EPSILON) break;
			// Show the iteration progress
			System.out.println("Iteration: " + i + ", ε: " + length);
			// Update the probability vector a with a = aP
			double dim = 1.0;
			double norm = this.norm(aP, dim);
			// Normalize vector a, to void round-off error
			a = Arrays.stream(aP).map(val -> val / norm).toArray();
		}
		System.out.println("======================================");

		// Create an array of pairs (value, index)
		int n = a.length; double[] val = a;
		Integer[] indexes = new Integer[n];
		for (int i = 0; i < n; i++) {
			indexes[i] = i;
		}

		// Sort the array of indexes based on the values in arr
		Arrays.sort(indexes, Comparator.comparingDouble(index -> val[(int) index]).reversed());

		// Print the 30 highest PageRank scores
		System.out.println("Print the 30 highest PageRank scores: ");
		System.out.println("======================================");
		for (int i = 0; i < 30; i++) {
			// Use the sorted indexes to access the sorted values in arr
			Integer idx = indexes[i];
			System.out.println(docName[idx] + ": " + a[idx]);
		}
		System.out.println("===================================");

		// Save the PageRank result in txt file
		try {
			FileWriter writer = new FileWriter(RESULT_DIR + "pagerank_result.txt");
			for (int i = 0; i < a.length; i++) {
				Integer idx = indexes[i];
				writer.write(docName[idx] + ": " + a[idx] + System.lineSeparator());
			}
			writer.close();
			System.out.println("Result saved in pagerank_result.txt");
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
    }

	// Calculate the distance between point a and b
	double distance(double[] a, double[] b) {
		double sum = 0.0;
		for (int j = 0; j < a.length; j++) {
			double sumComponent = a[j] - b[j];
			sum += sumComponent * sumComponent;
		}
        return Math.sqrt(sum);
    }

	// Calculate the modulus length of vector a
	double norm(double[] a, double dim) {
		double sum = 0.0;
        for (double sumComponent : a) {
            sum += Math.pow(sumComponent, dim);
        }
		return Math.pow(sum, 1.0 / dim);
	}

	/**
	 *  Multiply the vector a with transition matrix G
	 *  @return multiplication result aG where G = cP+(1-c)J
	 */
	double[] matrixVectorMul(double[] a) {
		// Initialize result vector aG
		double[] aG = new double[a.length];

		// Perform the multiplication
		for (int i = 0; i < a.length; i++) {
			double sum = 0;
			for (int j = 0; j < a.length; j++) {
				// Calculate P_ij
				double P_ij = 0;
				// If the page has outlinks
				if (this.link.get(j) != null) {
					if (this.link.get(j).get(i) != null)
						P_ij = 1.0 / this.out[j];
				}
				// If the page doesn't
				else {
					P_ij = 1.0 / a.length;
				}
				// Multiply each normalized element of the row
				// by the corresponding element of the vector a
				sum += ((1 - BORED) * P_ij + BORED / a.length) * a[j];
			}
			aG[i] = sum;
		}

		return aG;
	}

	// Perform Monte Carlo simulation of random walks on the graph
	private double[] MonteCarloSim1( int numberOfDocs ) {
		// An array to count the end of walks
		Integer[] walkEndCnt = new Integer[numberOfDocs];
		Arrays.fill(walkEndCnt, 0);
		// Start the N runs of the random walk
		for (int i = 0; i < this.maxIterations; i++) {
            // Start the random walk from a randomly chosen page
            int chosenPage = random.nextInt(numberOfDocs);
			while (true) {
                // Move to a random linked page or new page
                if (link.containsKey(chosenPage)) {
                    Integer[] outlinks = link.get(chosenPage).keySet().toArray(new Integer[0]);
                    chosenPage = outlinks[random.nextInt(outlinks.length)];
                } else {
                    chosenPage = random.nextInt(numberOfDocs);
                }
				// Terminate the walk with a certain probability 'BORED'
				if (random.nextDouble() < BORED) { // Exit Condition
					// Increment the count of walks ending at chosen page
					walkEndCnt[chosenPage] += 1;
					break;
				}
            }
        }
		return Arrays.stream(walkEndCnt)
					 .mapToDouble(i -> (double) i / numberOfDocs)
					 .toArray();
	}

    /* --------------------------------------------- */

    public static void main( String[] args ) {
		if ( args.length < 1 ) {
			System.err.println( "Please give the name of the link file" );
			return;
		}
		if ( args.length > 5 ) {
			System.err.println( "Too many arguments provided (MAX: 5)." );
		}

		int i = 1, methodID = 0, maxEpochs = 1;
		while ( i < args.length ) {
			switch (args[i++]) {
				case "-m":
					if (i < args.length) {
						try {
							methodID = Integer.parseInt(args[i++]);
							if ( methodID < 0 || methodID > 5 ) {
								throw new NumberFormatException();
							}
						} catch ( NumberFormatException e ) {
							System.err.println("Invalid input: '" + args[--i]
									+ "' is not a valid method id.");
							return;
						}
					}
					break;
				case "-r":
					if (i < args.length) {
						try {
							maxEpochs = Integer.parseInt(args[i++]);
							if ( maxEpochs < 1 ) {
								throw new NumberFormatException();
							}
						} catch ( NumberFormatException e ) {
							System.err.println("Invalid input: '" + args[--i]
									+ "' is not a valid epoch num.");
							return;
						}
					}
					break;
				default:
					System.err.println("Unknown option: " + args[i]);
					return;
			}
		}

		new PageRank( args[0], methodID, maxEpochs );
    }
}
