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
     *   Mapping from document numbers to document names
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

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

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
    /* --------------------------------------------- */


    public PageRank( String filename ) {
		int noOfDocs = readDocs( filename );
        int maxIterations = 1000;
        iterate( noOfDocs, maxIterations);
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
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
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
     */
    void iterate( int numberOfDocs, int maxIterations ) {
		//
		// YOUR CODE HERE
		//

		// Initialize vector a with length=1 and equal probabilities for all docs
		double[] a = new double[numberOfDocs];
		Arrays.fill(a, 1.0 / numberOfDocs);

		// Perform a fixed number of iterations
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

		// Create an array of pairs (value, index)
		int n = a.length; double[] val = a;
		Integer[] indexes = new Integer[n];
		for (int i = 0; i < n; i++) {
			indexes[i] = i;
		}

		// Sort the array of indexes based on the values in arr
		Arrays.sort(indexes, Comparator.comparingDouble(index -> val[index]));

		// Print the 30 highest PageRank scores
		for (int i = 0; i < 30; i++) {
			// Use the sorted indexes to access the sorted values in arr
			Integer idx = indexes[i];
			System.out.println(docName[idx] + ": " + a[idx]);
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


    /* --------------------------------------------- */

    public static void main( String[] args ) {
		if ( args.length != 1 ) {
			System.err.println( "Please give the name of the link file" );
		}
		else {
			new PageRank( args[0] );
		}
    }
}
