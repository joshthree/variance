#!/bin/bash
cd ..
./singleKeySetup.sh
./singleKeyProvisions.sh 1 127.0.01 5200 & ./singleKeyProvisions.sh 2 127.0.01 5200;
mkdir -p provisionsInputs
mkdir -p provisionsOutputs
cp ./inputs/* ./provisionsInputs
cp ./outputs/* ./provisionsOutputs
rm -rf outputs/*

./singleKeyVariance.sh 1 127.0.01 5200 & ./singleKeyVariance.sh 2 127.0.01 5200 ;
mkdir -p varianceInputs
mkdir -p varianceOutputs
cp ./inputs/* ./varianceInputs
cp ./outputs/* ./varianceOutputs

rm -rf outputs/*