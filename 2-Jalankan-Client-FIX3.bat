@echo off
cd /d "%~dp0"
title CLIENT KLINIK
color 0B
echo Folder kerja: %CD%
if not exist "dist\KlinikClientServer.jar" echo ERROR: File dist\KlinikClientServer.jar TIDAK KETEMU! & pause & exit
set JAVA_EXE=
for /D %%D in ("C:\Program Files\Java\jdk*" "C:\Program Files\Java\jre*" "C:\Program Files (x86)\Java\jdk*" "C:\Program Files (x86)\Java\jre*") do if exist "%%D\bin\java.exe" set JAVA_EXE="%%D\bin\java.exe"
if "%JAVA_EXE%"=="" set JAVA_EXE=java
echo Memakai Java: %JAVA_EXE%
echo Membuka Aplikasi Klinik...
echo Pastikan Server sudah jalan dulu!
%JAVA_EXE% -cp "dist\KlinikClientServer.jar;dist\lib\*" klinik.client.ui.LoginFrame
echo.
echo CLIENT BERHENTI. Error di atas adalah penyebabnya.
pause
