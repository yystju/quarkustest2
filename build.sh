#!/bin/bash
mvn clean antlr4:antlr4 install -DskipTests=true $@
