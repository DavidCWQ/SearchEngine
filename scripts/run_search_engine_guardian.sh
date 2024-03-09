#!/bin/sh
cd ../
java -cp target/classes -Xmx2g ir.Engine -d guardian -l dd2477.png -p patterns.txt
