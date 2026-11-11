#!/bin/sh
set -e

cd "$(dirname "$0")/.."

# Compile the benchmark and the application classes it uses.
javac -sourcepath src -d out test/BenchmarkTest.java

# Run the benchmark, passing along an optional source-file path.
java -cp out BenchmarkTest "$@"
