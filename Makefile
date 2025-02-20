JAVAC=javac
SRC=src
BIN=bin

all:
	mkdir -p bin
	cd bin/;mkdir -p protocol 
	cd bin/;mkdir -p zero_knowledge_proofs 
	cd bin/;mkdir -p zero_knowledge_proofs/CryptoData 
	
	cd src/;$(JAVAC) -cp "../jars/bcprov-ext-jdk15on-157.jar;." -d ../$(BIN)/protocol ./protocol/*.java
	cd src/;$(JAVAC) -cp "../jars/bcprov-ext-jdk15on-157.jar;." -d ../$(BIN)/zero_knowledge_proofs ./zero_knowledge_proofs/*.java
	cd src/;$(JAVAC) -cp "../jars/bcprov-ext-jdk15on-157.jar;." -d ../$(BIN)/zero_knowledge_proofs/CryptoData/ ./zero_knowledge_proofs/CryptoData/*.java
	
clean:
	rm -rf $(BIN)/*
	rm -rf $(BIN)

