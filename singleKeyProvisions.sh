#!/bin/bash
for n in 10 100 1000 10000 #100000 200000 300000 400000 500000 1000000 2000000 4000000 6000000 8000000 10000000
do
	echo "java -cp \"./jars/bcprov-ext-jdk15on-157.jar;./bin\" protocol.ProtocolMainProvisions $2 $3 Account${n}_0.05_0.5 P${1}Keys${n}_0.05_0.5 ecEnvironment 1"
	java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainProvisions $2 $3 Account${n}_0.05_0.5 P${1}Keys${n}_0.05_0.5 ecEnvironment 1
done
