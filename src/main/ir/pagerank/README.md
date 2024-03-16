# PageRank

The `PageRank.java` is a Java program that implements the PageRank algorithm, a link analysis algorithm used by search engines to rank web pages in search results. The program takes as input a file containing information about the links between web pages and computes the PageRank scores for each page based on its link structure.

## Overview

1. **Main Method**: The main method serves as the entry point for executing the PageRank algorithm. It reads command-line arguments, including the name of the link file, method ID, and maximum number of epochs.

2. **CLI Arguments**:

   - `-m`: Specifies the method ID for the PageRank algorithm. Valid values range from 0 to 5.

      ```text
      0: Power iteration
      1: MC end-point with random start
      2: MC end-point with cyclic start
      3: MC complete path
      4: MC complete path stopping at dangling nodes
      5: MC complete path with random start
      ```

   - `-r`: Specifies the maximum number of epochs (iterations) for the PageRank algorithm.

3. **Reading Input File**: The program reads the link file provided as input. This file contains information about the links between web pages. Each line represents a web page followed by its outbound links.
   
   ```text
   2;
   5;6,8,9,10,
   6;6792,13625,1370, ...
   ```
   
4. **PageRank Calculation**: Using the link information, the program computes the PageRank scores for each web page. It performs the PageRank algorithm or Monte Carlo approximation specified by method ID, iterating over the specified number of epochs.

5. **Output**: After computing the PageRank scores, the program prints the top-ranked pages along with their PageRank scores. Additionally, the results are saved to the `src/main/resources/` directory with a file named `[Link_File_Name]_rank_result.txt`.

## Usage

Compile the `PageRank.java` file and run the compiled program, providing the necessary command-line arguments:

```bash
java -cp target/classes -Xmx1g ir.pagerank.PageRank link_file_name [-options] [arguments]
```

### Options

- `-m`: Specifies the method ID for the PageRank algorithm (default: 0). Valid range is 0 to 5.

- `-r`: Specifies the maximum number of epochs (default: 1). Must be a positive integer.

## Example

```bash
java -cp target/classes -Xmx1g ir.pagerank.PageRank linksDavis.txt -m 1 -r 5
```

> This command runs the PageRank algorithm with method ID **1** and maximum epochs set to **5** using the link file `linksDavis.txt`.