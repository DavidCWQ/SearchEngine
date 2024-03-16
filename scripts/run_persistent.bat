cd ..\

java -cp target\classes -Xmx1g ir.Engine -d davisWiki -l dd2477.png -p patterns.txt -r pagerank_result.txt -t davisTitles.txt -lk linksDavis.txt -ni
