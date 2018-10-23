PREFIX=antlrgen/Parser
JFLAGS=-classpath ./src:./antlrgen:./:./lib/junit-4.12.jar:./lib/hamcrest-core-1.3.jar:./lib/antlr-4.5.3-complete.jar
JC=javac
CLASSES=./src/*.java ./src/nodes/*.java
ANTLR_FILES=Parser.g4
ANTLR_CLASSES=$(PREFIX)Lexer.java \
			  $(PREFIX)BaseVisitor.java \
			  $(PREFIX)Parser.java \
			  $(PREFIX)Visitor.java

all: java

java: antlrgen $(CLASSES)
	mkdir -p bin
	$(JC) $(JFLAGS) $(CLASSES) $(ANTLR_CLASSES) -d bin

run: all
	java $(JFLAGS):bin Compiler $(infile) $(cssfile) $(jsfile)

antlrgen: $(ANTLR_FILES)
	antlr4 -o antlrgen -no-listener -visitor -lib . $(ANTLR_FILES)

tests:
	java $(JFLAGS):bin org.junit.runner.JUnitCore CompilerTest

clean: 
	rm -rf antlrgen
	rm -rf bin
