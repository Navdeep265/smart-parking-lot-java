@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -cp lib\hsqldb.jar -d out src\main\java\parking\*.java src\test\java\parking\*.java
if errorlevel 1 exit /b 1
java -cp "out;lib\hsqldb.jar" parking.ParkingTest

if errorlevel 1 exit /b 1
java -cp "out;lib\hsqldb.jar" parking.StrategyTest
