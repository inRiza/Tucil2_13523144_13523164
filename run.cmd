@echo off

:: Tampilkan menu
echo [=================[ Menu Program Kompresi Gambar Quadtree ]=================]
echo 1. Compile dan Jalankan Program
echo 2. Compile Manual
echo 3. Jalankan Program
echo 4. Keluar
echo.

:: Input pilihan user
set /p choice="Pilih menu (1-4): "

:: Proses pilihan user
if "%choice%"=="1" (
    :: Compile dan jalankan program
    echo.
    echo [=================[ Compile dan Jalankan Program ]=================]
    
    :: Buat direktori bin jika belum ada
    if not exist "bin" mkdir "bin"
    
    :: Compile file utama
    echo Compiling...
    javac -d "bin" "src/app/*.java" "src/app/utils/*.java"
    
    :: Cek apakah kompilasi berhasil
    if errorlevel 1 (
        echo Error saat kompilasi!
        pause
        exit /b 1
    )
    
    echo Compile berhasil!
    echo.
    echo Menjalankan program...
    echo.
    
    :: Jalankan program utama
    java -cp "bin" src.app.Main
    
) else if "%choice%"=="2" (
    :: Compile manual
    echo.
    echo [=================[ Compile Manual ]=================]
    
    :: Buat direktori bin jika belum ada
    if not exist "bin" mkdir "bin"
    
    :: Compile file utama
    javac -d "bin" "src/app/*.java" "src/app/utils/*.java"
    
    echo Compile selesai!
    
) else if "%choice%"=="3" (
    :: Jalankan program
    echo.
    echo [=================[ Jalankan Program ]=================]
    
    :: Cek apakah file class ada
    if not exist "bin\src\app\Main.class" (
        echo Error: Program belum di-compile!
        echo Silakan pilih opsi 1 atau 2 untuk compile terlebih dahulu.
        pause
        exit /b 1
    )
    
    :: Jalankan program utama
    java -cp "bin" src.app.Main
    
) else if "%choice%"=="4" (
    :: Keluar
    echo.
    echo Terima kasih telah menggunakan program ini!
    exit /b 0
    
) else (
    :: Pilihan tidak valid
    echo.
    echo Pilihan tidak valid!
    pause
    exit /b 1
)

pause

:: Cara Penggunaan
:: ./run.cmd