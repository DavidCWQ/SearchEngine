cd ..\

if not exist target\classes (
    mkdir target\classes
)

javac -cp ".;src\main\lib\jcommon-1.0.23.jar;src\main\lib\jfreechart-1.0.19.jar" ^
    -d target\classes ^
    src\main\ir\Engine.java ^
    src\main\ir\HashedIndex.java ^
    src\main\ir\HITSRanker.java ^
    src\main\ir\Index.java ^
    src\main\ir\Indexer.java ^
    src\main\ir\KGramIndex.java ^
    src\main\ir\KGramPostingsEntry.java ^
    src\main\ir\NormalizationType.java ^
    src\main\ir\PersistentHashedIndex.java ^
    src\main\ir\PostingsEntry.java ^
    src\main\ir\PostingsList.java ^
    src\main\ir\Query.java ^
    src\main\ir\QueryType.java ^
    src\main\ir\RankingType.java ^
    src\main\ir\Searcher.java ^
    src\main\ir\SearchGUI.java ^
    src\main\ir\SpellChecker.java ^
    src\main\ir\SpellingOptionsDialog.java ^
    src\main\ir\Tokenizer.java ^
    src\tests\TokenTest.java ^
    src\main\ir\pagerank\PageRank.java ^
    src\main\ir\pagerank\PageRankPlotting.java