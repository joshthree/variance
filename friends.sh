#!/bin/bash
for n in `seq 2 10`
do
	for k in `seq 2 $n`
	do
		let friends=k-2;
		echo n = $n, k = $k
		for i in `seq 1 $friends`;
		do
			echo java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigFriend $2 $3 `expr $i - 1` Account${4}_0.05_0.5_${n}_${k} P${1}.${i}_Keys${4}_0.05_0.5_${n}_${k} ecEnvironment
			java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigFriend $2 $3 `expr $i - 1` Account${4}_0.05_0.5_${n}_${k} P${1}.${i}_Keys${4}_0.05_0.5_${n}_${k} ecEnvironment &
		done
		echo java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigFriend $2 $3 `expr $k - 2` Account${4}_0.05_0.5_${n}_${k} P${1}.`expr $k - 1`_Keys${4}_0.05_0.5_${n}_${k} ecEnvironment
		java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigFriend $2 $3 `expr $k - 2` Account${4}_0.05_0.5_${n}_${k} P${1}.`expr $k - 1`_Keys${4}_0.05_0.5_${n}_${k} ecEnvironment
	done
done
