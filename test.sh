#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
javac -cp lib/hsqldb.jar -d out src/main/java/parking/*.java src/test/java/parking/*.java
java -cp out:lib/hsqldb.jar parking.ParkingTest

java -cp out:lib/hsqldb.jar parking.StrategyTest
