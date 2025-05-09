#!/bin/bash

for n in 10 100 1000 10000 #100000 200000 300000 400000 500000 1000000 2000000 4000000 6000000 8000000 10000000
do
	java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ECKeyMaker secp256k1 $n 0.05 0.5 
done

