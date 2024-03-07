#!/bin/sh

java -cp ../target/classes -Xmx2g ir.Engine -d ../src/main/datasets/davisWiki -l dd2477.png -p patterns.txt
