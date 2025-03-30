@echo off

:: Cari semua file .java
dir /s /b src\*.java > sources.txt

:: Compile semua file ke bin/
javac -d bin -cp bin @sources.txt

:: Hapus file sementara
del sources.txt

:: Jalankan program utama
java -cp bin src.app.Main

:: Hapus semua file .class dalam bin/
for /r bin %%f in (*.class) do del "%%f"

:: Hapus semua folder kosong dalam bin/
for /d /r bin %%d in (*) do rd "%%d" 2>nul

:: Cara Penggunaan
:: ./run.cmd