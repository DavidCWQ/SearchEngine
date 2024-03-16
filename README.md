# Information Retrieval System

## Overview

Welcome to this student repository for the KTH DD2477 Search Engine course project!

This repository contains the source code skeleton for implementing a rudimentary search engine as part of the assignments for the KTH DD2477 Search Engine course. The objective of this project is to develop a basic search engine that will be evaluated on a corpus of linked documents from the wiki for the US town Davis. The dataset can be found at [DavisWiki](https://daviswiki.org/).

Through this project, I have the opportunity to learn and apply concepts related to information retrieval, indexing, ranking algorithms, and search engine implementation. The codebase provided in this repository serves as the foundation for implementing the search engine functionalities across three assignments, with each assignment building upon the previous one.

For detailed instructions on each assignment, please refer to the `tasks` folder in this repository. The code base were created by Prof. Johan Boye et al., refactored and implemented by David Cao. 

## Dataset

Before getting started, ensure you have downloaded the datasets in this repository:
- `davisWiki` Dataset
- `guardian` Dataset

Unzip them in the `src/main/datasets` directory.

## Getting Started

### Prerequisites
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) installed on your system (Java 8 and above)
- [Git](https://git-scm.com/) installed on your system (if cloning the repository)

### Installation

1. Clone or download this repository to your local machine:

   ```bash
   git clone https://github.com/DavidCWQ/SearchEngine.git
   ```

   > Alternatively, download the repository as a ZIP file and extract it to a directory of your choice.

2. Navigate to the directory containing the cloned or extracted files:

   ```bash
   cd SearchEngine
   ```

   Navigate to the directory containing the project scripts:

   ```bash
   cd scripts
   ```

3. If you're on a Unix-like system (Linux, macOS), compile the lab skeleton by running:

   ```bash
   sh compile_all.sh
   ```

   If you're on a Windows platform, you can use the batch file instead:

   ```bash
   ./compile_all.bat
   ```

   You may encounter some warnings during compilation; these can typically be ignored.

### Usage & Examples

Once the compilation is successful, you can run the search engine using the provided scripts. Here's an example script command to get started:

```bash
cd ..
```

```bash
java -cp target/classes -Xmx1g ir.Engine -d davisWiki -l dd2477.png -p patterns.txt -r pagerank_result.txt -t davisTitles.txt -lk linksDavis.txt
```

You can also run the search engine with a persistent index if you have the dataset indexed:

```bash
java -cp target/classes -Xmx1g ir.Engine -d davisWiki -l dd2477.png -p patterns.txt -r pagerank_result.txt -t davisTitles.txt -lk linksDavis.txt -ni
```

Please remember to recompile the project after making any changes to the source code. 

> You can do this by running the `compile_all.sh` script (for Unix-like systems) or `compile_all.bat` batch file (for Windows) located in the `scripts` directory.

### Command Line Options

The program supports the following command-line options:

- `-d [dataset_name]`: Specify the dataset name (`davisWiki` by default).

- `-p [pattern_file]`: Specify the `regex` file used in tokenization.

- `-l [search_logo]`: Specify the project logo file `dd2477.png`.

- `-r [rank_result]`: Specify the pagerank result file.

- `-t [rank_title]`: Specify the pagerank title file.

- `-lk [link_file]`: Specify the pagerank link file.

- `-ni`: Disable indexing.

---

### Directory Structure

Here's an overview of the directory structure for this project.

```
.
├── scripts/                 # Directory for scripts related to project automation
├── src/                     # Source code files
│   ├── main/                # Main application code
│   │   ├── datasets/        # Directory for dataset files used in the project
│   │   ├── ir/              # Java source files for Information Retrieval components
│   │   ├── lib/             # Third-party libraries used in the project
│   │   └── resources/       # Additional resource files used in the application
│   └── test/                # Test code (partial)
│       └── resources/       # Resource files used in test cases
├── target/                  # Compiled output directory
├── tasks/                   # Directory for task-related documentation and assignment PDF
└── README.md                # Project README file providing an overview of the project
```

Feel free to explore each directory for more details on the contents.

## License

This project is licensed under the [Apache 2.0 License](LICENSE).

## Contributing

Contributions to this project are welcome. Feel free to submit bug reports, feature requests, or pull requests.

## Acknowledgements

Special thanks to KTH DD2477 course instructors and TAs for providing the course materials and guidance.

