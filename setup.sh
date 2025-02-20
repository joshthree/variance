#!/bin/bash
for n in `seq 1 10`;
do
	for k in `seq 1 $n`;
	do
		echo "java -cp \"./jars/bcprov-ext-jdk15on-157.jar;./bin\" protocol.ECKeyMakerMultiSigWithFriends secp256k1 $1 $n $k 0.05 0.5"
		java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ECKeyMakerMultiSigWithFriends secp256k1 $1 $n $k 0.05 0.5
	done
done
