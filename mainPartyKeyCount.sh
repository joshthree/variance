#!/bin/bash
for n in `seq 1 10`
do
	for k in `seq 1 $n`
	do
		echo n = $n, k = $k
		echo java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigMainPartyKeyCount $2 $3 $4 Account${5}_0.05_0.5_${n}_${k} P${1}.0_Keys${5}_0.05_0.5_${n}_${k} ecEnvironment 1 P${1}_`expr $k - 1`_friend	
		java -cp "./jars/bcprov-ext-jdk15on-157.jar;./bin" protocol.ProtocolMainVarianceMultiSigMainPartyKeyCount $2 $3 $4 Account${5}_0.05_0.5_${n}_${k} P${1}.0_Keys${5}_0.05_0.5_${n}_${k} ecEnvironment 1 P${1}_`expr $k - 1`_friend	

	done
done
