#!/bin/sh

java -cp ../target/classes -Xmx2g ir.Engine -d ../src/main/datasets/guardian -l dd2477.png -p patterns.txt
