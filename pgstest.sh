#!/bin/bash

classpath=.:bin:lib/*

echo "Launching PGSTest..."

java -classpath $classpath -Djava.library.path=lib/ main.PGSvsScripts "$@" 

echo "Done."
