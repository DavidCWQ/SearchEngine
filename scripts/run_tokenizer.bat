cd ..\

java -cp target\classes ir.TokenTest -f tests\resources\token_test.txt -p patterns.txt -rp -cf > tests\tokenized_result.txt
