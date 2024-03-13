package ir.pagerank;

import java.util.*;
import java.io.*;
import java.lang.Math;

public class PageRank {

    /**
     *   Maximal number of documents. We're assuming that we
     *   don't have more docs than we can keep in main memory.
     */
    private static final int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    private final HashMap<String,Integer> docNumber = new HashMap<>();

    /**
     *   Mapping from document numbers to document names.
     */
	private final String[] docName = new String[MAX_NUMBER_OF_DOCS];

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
	private final HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<>();

    /** The number of outlinks from each node. */
	private final int[] out = new int[MAX_NUMBER_OF_DOCS];

	/** The filename of linkFile.*/
	private String fileName;

	/** Max number of iterations or rounds of random walk. */
	private int maxIterations;

	/** Create and initialize a thread-safe random number generator. */
	private final Random random = new Random();

	/** The pagerank scores result after calculation. */
	private double[] scores = null;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    private double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    private double EPSILON = 0.0001;

	/** The directory where the pagerank related files are stored. */
	private static final String PAGERANK_DIR = "src/main/ir/pagerank/";

	/** The directory where the pagerank result are stored. */
	private static final String RESULT_DIR = "src/main/resources/";
    /* --------------------------------------------- */

    public PageRank(File linkFile, Integer maxEpochs) {
		readDocs( linkFile );
		setMaxEpochs( maxEpochs );
    }

	/**
	 *  Start the pagerank calculation using given method.
	 *  @param method_id given method id
	 *                   0: Power iteration
	 *                   1: MC end-point with random start
	 *                   2: MC end-point with cyclic start
	 *                   3: MC complete path
	 *                   4: MC complete path stopping at dangling nodes
	 *                   5: MC complete path with random start
	 *  @param save_to_file save the result to txt if true
	 *  @return the top 30 documents with the highest rank
	 */
	public Integer[] runPageRank ( int method_id, boolean save_to_file ) {
		switch ( method_id ) {
			case 1:
				scores = MonteCarloSim1();
				break;
			case 2:
				scores = MonteCarloSim2();
				break;
			case 4:
				scores = MonteCarloSim4();
				break;
			case 5:
				scores = MonteCarloSim5();
				break;
			default:
				scores = iterate();
				break;
		}
		Integer[] indexes = this.sortScores( false );
		if ( save_to_file ) saveToFile();
		return Arrays.copyOf( indexes, Math.min(indexes.length, 30) );
	}

	/**
	 *  Sorts the scores in descending order by default and returns
	 *  the sorted indexes.
	 *  @param ascending in ascending order if set to true
	 *  @return an array of int representing the sorted indexes of
	 *  the scores array.
	 */
	public Integer[] sortScores( boolean ascending ) {
		if (scores == null) { return new Integer[0]; }

		// Create an array of pairs (value, index)
		int n = scores.length;
		Integer[] indexes = new Integer[n];
		for (int i = 0; i < n; i++) {
			indexes[i] = i;
		}

		// Sort the array of indexes based on the values in arr
		Arrays.sort(indexes, ascending ?
				Comparator.comparingDouble(index -> scores[index]) :
				Comparator.comparingDouble(index -> scores[(int) index]).reversed());

		return indexes;
	}

	/**
	 *  Prints the PageRank scores for the specified number in sequence
	 *  of indexes.
	 *  @param seq An array containing the score printing sequence.
	 */
	public void printScores(Integer[] seq) {
		System.out.println("Print " + seq.length + " PageRank scores: ");
		System.out.println("======================================");
		if ( scores == null ) { return; }
		int num = Math.min(seq.length, scores.length);
		int border = scores.length - 1;
		for (int i = 0; i < num; i++) {
			// Use the sorted indexes to access the sorted values in arr
			Integer idx = seq[i];
			if ( idx < 0 || idx > border ) {
				System.err.println("Error in PrintScores: " +
						"array value of the given seq is outside the valid document range.");
				return;
			}
			System.out.println(docName[idx] + ": " + scores[idx]);
		}
		System.out.println("======================================");
	}

	/**
	 *  Saves the PageRank results to a text file.
	 *  The results are written in the format "&lt;document_name&gt;:
	 *  &lt;PageRank_score&gt;".<p>
	 *  If an error occurs during writing, an error message will be
	 *  printed to the standard error stream.
	 */
	public void saveToFile() {
		if (fileName.isEmpty()) { return; }
		try {
			FileWriter writer = new FileWriter(RESULT_DIR + fileName + "_rank_result.txt");
			if (scores == null) { writer.close(); return; }
			for (int i = 0; i < scores.length; i++) {
				writer.write(docName[i] + ": " + scores[i] + System.lineSeparator());
			}
			writer.close();
			System.out.println("Result saved in pagerank_result.txt");
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}
	}


    /* --------------------------------------------- */

	/** Reset the max epochs of iterations. */
	public void setMaxEpochs( int newEpochs ) {
		this.maxIterations = newEpochs * docNumber.size();
	}

	/** Reset the probability of getting bored. */
	public void setBoardProb( double boardProb ) {
		this.BORED = boardProb;
	}

	/** Reset the convergence criterion parameter. */
	public void setEpsilon( double epsilon ) {
		this.EPSILON = epsilon;
	}

	/** Return a copy of the scores array. */
	public double[] getScores() {
		if (scores == null) { return new double[0]; }
		return Arrays.copyOf(this.scores, this.scores.length);
	}

	private void clearDocs() {
		this.link.clear();
		this.docNumber.clear();
		Arrays.fill(this.out, 0);
		Arrays.fill(this.docName, "");
		this.scores = null;
	}

	/**
	 *   Reads the documents and fills the data structures.
	 *   Previous doc stored will be cleared.
	 *   @param linkFile the doc containing the link structure of pages
	 */
	void readDocs( File linkFile ) {
		clearDocs();
		int fileIndex = 0;
		try {
			String line;
			BufferedReader in = new BufferedReader( new FileReader( linkFile ));
			while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS ) {
				int idx = line.indexOf( ";" );
				String title = line.substring( 0, idx );
				Integer fromDoc = docNumber.get( title );
				// Have we seen this document before?
				if ( fromDoc == null ) { // a previously unseen doc
					// add it to the table
					fromDoc = fileIndex++;
					docName[fromDoc] = title;
					docNumber.put( title, fromDoc );
				}
				// Check all outlinks.
				StringTokenizer tok = new StringTokenizer(
						line.substring(idx + 1), "," );
				while ( tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS ) {
					String otherTitle = tok.nextToken();
					Integer otherDoc = docNumber.get( otherTitle );
					if ( otherDoc == null ) { // a previously unseen doc
						// add it to the table
						otherDoc = fileIndex++;
						docName[otherDoc] = otherTitle;
						docNumber.put( otherTitle, otherDoc );
					}
					// Set the probability to 0 for now, to indicate that there is
					// a link from fromDoc to otherDoc.
					link.computeIfAbsent( fromDoc, k -> new HashMap<>() );
					if ( link.get(fromDoc).get(otherDoc) == null ) {
						link.get(fromDoc).put( otherDoc, true );
						out[fromDoc]++;
					}
				}
			}
			if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
				System.err.print( "Stopped reading since documents table is full. " );
			}
			else {
				System.err.print( "Reading completed." );
			}
		}
		catch ( FileNotFoundException e ) {
			System.err.println( "Error finding linkFile: " + e.getMessage() );
		}
		catch ( IOException e ) {
			System.err.println( "Error reading linkFile: " + e.getMessage() );
		}
		fileName = linkFile.getName();
		System.err.println( "Read " + fileIndex + " number of documents." );
	}


    /* --------------------------------------------- */


    /**
     *  Chooses a probability vector a, and repeatedly computes
     *  aP, aP^2, aP^3... until aP^i = aP^(i+1).
	 *  Save the result as txt file named "pagerank_result.txt"
     */
    private double[] iterate() { // Power iteration
		// YOUR CODE HERE
		int numberOfDocs = docNumber.size();
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
		return a;
    }

	// Calculate the distance between point a and b
	private double distance(double[] a, double[] b) {
		double sum = 0.0;
		for (int j = 0; j < a.length; j++) {
			double sumComponent = a[j] - b[j];
			sum += sumComponent * sumComponent;
		}
        return Math.sqrt(sum);
    }

	// Calculate the modulus length of vector a
	private double norm(double[] a, double dim) {
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
	private double[] matrixVectorMul(double[] a) {
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

	/**
	 *  Perform Monte Carlo simulation of random walks.
	 *  MC end-point with random start.
	 */
	private double[] MonteCarloSim1() {
		int numberOfDocs = docNumber.size();
		int[] walkEndCnt = new int[numberOfDocs];
		// Start the N runs of the random walk
		for (int i = 0; i < this.maxIterations; i++) {
            // Start the random walk from a randomly chosen page
			MonteCarloWalk( walkEndCnt, random.nextInt(numberOfDocs), false );
        }
		return Arrays.stream(walkEndCnt)
					 .mapToDouble(i -> (double) i / maxIterations)
					 .toArray();
	}

	/**
	 *  Perform Monte Carlo simulation of random walks.
	 *  MC end-point with cyclic start.
	 */
	private double[] MonteCarloSim2() {
		int numberOfDocs = docNumber.size();
		int[] walkEndCnt = new int[numberOfDocs];
		// Start the N = mn runs of the random walk
		for (int i = 0; i < maxIterations / numberOfDocs; i++) {
			// Start the random walk from a cyclically chosen page
			for (int p = 0; p < numberOfDocs; p++) {
				MonteCarloWalk( walkEndCnt, p, false );
			}
		}
		return Arrays.stream(walkEndCnt)
				.mapToDouble(i -> (double) i / maxIterations)
				.toArray();
	}

	/**
	 *  Performs a Monte Carlo walk simulation.
	 *
	 *  @param count        An array to count the visits or end of walks for each page.
	 *  @param chosenPage   The starting page for the walk.
	 *  @param completePath Flag indicating whether to stop at dangling nodes.
	 *                      True: stopped at dangling nodes; False: otherwise.
	 */
	private void MonteCarloWalk( int[] count, int chosenPage, boolean completePath ) {
		boolean reachDangling = false;
		while (true) {
			// Move to a random linked page or new page
			if (link.containsKey(chosenPage)) {
				Integer[] outlinks = link.get(chosenPage).keySet().toArray(new Integer[0]);
				chosenPage = outlinks[random.nextInt(outlinks.length)];
			}
			else if (completePath) {
				reachDangling = true;
			}
			else {
				chosenPage = random.nextInt(docNumber.size());
			}
			// Terminate the walk with a certain probability 'BORED'
			if (random.nextDouble() < BORED || reachDangling) { // Exit Condition
				// Increment the count of walks ending at chosen page
				count[chosenPage] += 1;
				break;
			}
			if (completePath) {
				count[chosenPage] += 1;
			}
		}
	}

	/**
	 *  Perform Monte Carlo simulation of random walks.
	 *  MC complete path stopping at dangling nodes.
	 */
	private double[] MonteCarloSim4() {
		int numberOfDocs = docNumber.size();
		int[] visitCount = new int[numberOfDocs];
		// Start the N = mn runs of the random walk
		for (int i = 0; i < maxIterations / numberOfDocs; i++) {
			// Start the random walk from a cyclically chosen page
			for (int p = 0; p < numberOfDocs; p++) {
				MonteCarloWalk( visitCount, p, true );
			}
		}
		return Arrays.stream(visitCount)
				.mapToDouble(i -> (double) i / Arrays.stream(visitCount).sum())
				.toArray();
	}

	/**
	 *  Perform Monte Carlo simulation of random walks.
	 *  MC complete path with random start.
	 */
	private double[] MonteCarloSim5() {
		int numberOfDocs = docNumber.size();
		int[] visitCount = new int[numberOfDocs];
		// Start the N runs of the random walk
		for (int i = 0; i < this.maxIterations; i++) {
			// Start the random walk from a randomly chosen page
			MonteCarloWalk( visitCount, random.nextInt(numberOfDocs), true );
		}
		return Arrays.stream(visitCount)
				.mapToDouble(i -> (double) i / Arrays.stream(visitCount).sum())
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

		try {
			System.err.print( "Reading file... " );
			File file = new File( PAGERANK_DIR + args[0] );
			PageRank PRCalculator = new PageRank( file, maxEpochs );
			Integer[] docIDs = PRCalculator.runPageRank(methodID, true);
			PRCalculator.printScores(docIDs);
		}
		catch ( NullPointerException e ) {
			System.err.println( "Error of Null Pointer: " + e.getMessage() );
		}
    }
}
