#!/bin/sh
cd ../
java -cp target/classes ir.TokenTest -f token_test.txt -p patterns.txt -rp -cf > src/tests/tokenized_result.txt
