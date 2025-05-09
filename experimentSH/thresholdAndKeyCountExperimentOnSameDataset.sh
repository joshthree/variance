#!/bin/bash
cd ..
make clean;make


num=100

./setup.sh $num
./mainParty.sh 1 127.0.01 5200 5201 $num & ./mainParty.sh 2 127.0.01 5201 5200 $num & ./friends.sh 1 127.0.0.1 5201 $num & ./friends.sh 2 127.0.0.1 5200 $num ;
mkdir -p thresholdInputs
mkdir -p thresholdOutputs
cp -r ./inputs/* ./thresholdInputs
cp -r ./outputs/* ./thresholdOutputs
rm -rf outputs/*

./mainPartyKeyCount.sh 1 127.0.01 5200 5201 $num & ./mainPartyKeyCount.sh 2 127.0.01 5201 5200 $num & ./friends.sh 1 127.0.0.1 5201 $num & ./friends.sh 2 127.0.0.1 5200 $num ;
mkdir -p keyCountInputs
mkdir -p keyCountOutputs
cp -r ./inputs/* ./keyCountInputs
cp -r ./outputs/* ./keyCountOutputs

rm -rf outputs/*