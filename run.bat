@echo off
title StudyFlow Student Hub
if not exist out mkdir out
echo Compiling StudyFlow...
javac -encoding UTF-8 -d out src\StudyFlow.java
if errorlevel 1 (
 echo.
 echo Compilation failed. Check JDK 17 or newer is installed.
 pause
 exit /b 1
)
echo Launching StudyFlow...
java -cp out StudyFlow
pause
