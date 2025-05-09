#!/bin/bash
cd ..

num=10000

make clean;make
./setup.sh $num
./mainParty.sh 1 127.0.01 5200 5201 $num & ./mainParty.sh 2 127.0.01 5201 5200 $num & ./friends.sh 1 127.0.0.1 5201 $num & ./friends.sh 2 127.0.0.1 5200 $num ;
mkdir -p thresholdInputs
mkdir -p thresholdOutputs
cp -r ./inputs/* ./thresholdInputs
cp -r ./outputs/* ./thresholdOutputs

rm -rf ./outputs/*