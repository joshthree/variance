#!/bin/bash
cd ..
make clean;make

num=10000

./setup.sh $num
./mainPartyKeyCount.sh 1 127.0.01 5200 5201 $num & ./mainPartyKeyCount.sh 2 127.0.01 5201 5200 $num & ./friends.sh 1 127.0.0.1 5201 $num & ./friends.sh 2 127.0.0.1 5200 $num ;
mkdir -p keyCountInputs
mkdir -p keyCountOutputs
cp -r ./inputs/* ./keyCountInputs
cp -r ./outputs/* ./keyCountOutputs


rm -rf ./outputs/*