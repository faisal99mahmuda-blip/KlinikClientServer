@echo off
cd /d "%~dp0"
title SERVER KLINIK - Jangan Ditutup
color 0A
echo Folder kerja: %CD%
if not exist "dist\KlinikClientServer.jar" echo ERROR: File dist\KlinikClientServer.jar TIDAK KETEMU! & pause & exit
if not exist "dist\lib" echo WARNING: Folder dist\lib TIDAK ADA!
set JAVA_EXE=
for /D %%D in ("C:\Program Files\Java\jdk*" "C:\Program Files\Java\jre*" "C:\Program Files (x86)\Java\jdk*" "C:\Program Files (x86)\Java\jre*") do if exist "%%D\bin\java.exe" set JAVA_EXE="%%D\bin\java.exe"
if "%JAVA_EXE%"=="" set JAVA_EXE=java
echo Memakai Java: %JAVA_EXE%
echo SERVER KLINIK JALAN di port 5000...
echo Kalau port 5000 sudah dipakai, tutup NetBeans / Server lama dulu!
%JAVA_EXE% -cp "dist\KlinikClientServer.jar;dist\lib\*" klinik.server.KlinikServer
echo.
echo SERVER BERHENTI. Error di atas adalah penyebabnya.
pause
