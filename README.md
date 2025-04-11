# Program Kompresi Gambar Quadtree

## Deskripsi
Program ini merupakan implementasi algoritma kompresi gambar canggih yang menggunakan struktur data Quadtree sebagai dasar kompresinya. Program ini dirancang untuk memberikan solusi kompresi gambar yang optimal dengan mempertahankan kualitas visual yang baik sambil mengurangi ukuran file secara signifikan. Dengan berbagai metode pengukuran error yang tersedia, pengguna dapat memilih pendekatan yang paling sesuai dengan kebutuhan mereka.

## Fitur Utama
1. Lima Metode Pengukuran Error yang Berbeda:
   - Variance (Varians): Mengukur variasi intensitas warna
   - MAD (Mean Absolute Deviation): Mengukur deviasi rata-rata
   - Max Pixel Difference: Mengukur perbedaan maksimum piksel
   - Entropy: Mengukur kompleksitas informasi
   - SSIM (Structural Similarity Index): Mengukur kesamaan struktural

2. Visualisasi Proses Kompresi:
   - Generasi GIF otomatis yang menunjukkan proses kompresi
   - Visualisasi pembagian blok Quadtree
   - Animasi proses kompresi yang interaktif

3. Laporan Hasil Kompresi:
   - Perbandingan ukuran file sebelum dan sesudah kompresi
   - Rasio kompresi yang dicapai
   - Waktu yang dibutuhkan untuk proses kompresi
   - Detail struktur Quadtree hasil kompresi

4. Dukungan Format Gambar:
   - JPG/JPEG
   - PNG
   - Dapat menangani gambar dengan berbagai resolusi

## Cara Penggunaan

### Prasyarat
- Java Runtime Environment (JRE) versi 8 atau lebih baru
- Sistem operasi Windows (untuk run.cmd)

### Langkah-langkah Menjalankan Program
1. Buka terminal/command prompt
2. Navigasi ke direktori program
3. Jalankan program dengan perintah:
   ```
   ./run.cmd
   ```

### Proses Kompresi
1. Masukkan Path Gambar:
   - Masukkan path lengkap gambar yang ingin dikompres
   - Pastikan format gambar didukung (JPG, JPEG, PNG)

2. Pilih Metode Error:
   - Pilih metode pengukuran error (1-5)
   - Setiap metode memiliki karakteristik berbeda:
     * 1: Variance - Cocok untuk gambar dengan variasi warna tinggi
     * 2: MAD - Baik untuk gambar dengan perubahan halus
     * 3: Max Pixel Difference - Ideal untuk gambar dengan kontras tinggi
     * 4: Entropy - Efektif untuk gambar kompleks
     * 5: SSIM - Optimal untuk mempertahankan kualitas visual

3. Tentukan Parameter:
   - Threshold Error: Nilai yang menentukan kapan blok perlu dibagi
   - Ukuran Blok Minimum: Batas terkecil ukuran blok yang diizinkan

4. Tentukan Lokasi Output:
   - Pilih direktori untuk menyimpan hasil kompresi
   - Program akan membuat file baru dengan format: nama_gambar_compressed_X.ext

## Penjelasan Metode Pengukuran Error

### 1. Variance (Varians)
Metode ini mengukur seberapa beragam intensitas warna dalam suatu blok gambar. Semakin tinggi varians, semakin beragam warna dalam blok tersebut. Cocok digunakan untuk gambar yang memiliki banyak variasi warna dalam area kecil.

### 2. MAD (Mean Absolute Deviation)
MAD mengukur rata-rata deviasi absolut dari nilai rata-rata warna dalam blok. Metode ini sangat sensitif terhadap perubahan halus dalam gambar dan cocok untuk gambar dengan gradasi warna yang halus.

### 3. Max Pixel Difference
Metode ini fokus pada perbedaan maksimum antara nilai piksel dalam blok. Sangat efektif untuk mendeteksi kontras tinggi dalam gambar dan cocok untuk gambar dengan tepian yang tajam.

### 4. Entropy
Entropy mengukur kompleksitas informasi dalam blok gambar. Semakin tinggi entropi, semakin kompleks informasi dalam blok tersebut. Metode ini ideal untuk gambar dengan detail yang rumit.

### 5. SSIM (Structural Similarity Index)
SSIM adalah metode canggih yang mengukur kesamaan struktural antara blok asli dan hasil kompresi. Metode ini mempertimbangkan tiga aspek:
- Luminance: Kecerahan gambar
- Contrast: Perbedaan intensitas
- Structure: Pola dan tekstur

## Struktur Proyek
```
src/
├── app/
│   ├── ErrorMeasurement.java    # Implementasi semua metode pengukuran error
│   ├── ImageProcessor.java      # Logika utama pemrosesan gambar
│   ├── Main.java                # Program utama dan antarmuka pengguna
│   ├── QuadTreeNode.java        # Implementasi struktur data Quadtree
│   └── utils/                   # Kelas utilitas pendukung
       ├── ImageUtils.java       # Utilitas pemrosesan gambar
       └── GIFGenerator.java     # Generator animasi proses kompresi
```

## Kontribusi
Program ini dikembangkan oleh:
- Muhammad Nazih Najmudin (13523144)
- Muhammad Rizain Firdaus (13523164)

## Lisensi
Program ini dilisensikan di bawah MIT License. Lihat file LICENSE untuk detail lebih lanjut.

## Ucapan Terima Kasih
Kami mengucapkan terima kasih kepada:
- Dosen dan asisten mata kuliah IF2211 Strategi Algoritma
- Tim pengembang yang telah berkontribusi dalam pengembangan program
- Semua pihak yang telah memberikan masukan dan saran

---

Dibuat untuk memenuhi Tugas Kecil 2 IF2211 Strategi Algoritma
