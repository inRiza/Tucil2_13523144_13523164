package src.app;

import src.app.utils.ImageUtils;
import src.app.utils.GIFGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Clear Terminal
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("[=================[ Program Kompresi Gambar Quadtree ]=================]");

            // 1. Input nama file (tanpa path)
            System.out.print("\n[input] Masukkan nama absolute path gambar \n>> ");
            String filename = scanner.nextLine();
            File inputFile = new File(filename);
            long inputSize = inputFile.length();

            // Validasi file exist
            if (!inputFile.exists()) {
                System.err.println("[Error] File tidak ditemukan di " + inputFile.getAbsolutePath());
                return;
            }

            // 2. Input parameter kompresi
            System.out.println("\n[input] Pilih metode error: ");
            System.out.println("\t1. Variance\n\t2. MAD\n\t3. Max Pixel Difference\n\t4. Entropy");
            System.out.print(">> Pilihan (1-4): ");
            int method = scanner.nextInt();

            System.out.print("\n[input] Masukkan Threshold error (contoh: 10.0) \n>> ");
            double threshold = scanner.nextDouble();

            System.out.print("\n[input] Masukkan Ukuran blok minimum (contoh: 4) \n>> ");
            int minBlockSize = scanner.nextInt();

            // 3. Proses gambar
            BufferedImage image = ImageUtils.readImage(inputFile.getAbsolutePath());

            // Buat nama output otomatis
            System.out.print("\n[input] Masukkan alamat absolut output gambar \n>> ");
            scanner.nextLine();
            String outputDirPath = scanner.nextLine();

            // Buat nama file output otomatis
            String inputFileName = inputFile.getName();
            String extension = inputFileName.substring(inputFileName.lastIndexOf("."));
            String baseName = inputFileName.substring(0, inputFileName.lastIndexOf("."));
            String outputFileName = baseName + "_out" + extension;
            String outputFilename = outputDirPath + File.separator + outputFileName;

            File outputFile = new File(outputFilename);

            // Check if output directory exists
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                System.out.println(
                        "Sepertinya alamat absolut untuk penyimpanan proses gambar yang diberikan belum ada, maukah saya buatkan alamat absolutnya?(y/n)");
                String response = scanner.nextLine().toLowerCase();
                if (response.equals("y")) {
                    if (outputDir.mkdirs()) {
                        System.out.println("Berhasil membuat tempat penyimpanan");
                    } else {
                        System.out.println("Gagal menyimpan gambar");
                        return;
                    }
                } else {
                    System.out.println("Gagal menyimpan gambar");
                    return;
                }
            }

            // Inisialisasi GIF Generator
            GIFGenerator gifGen = new GIFGenerator(500); // Delay default 500ms

            // Proses kompresi
            Instant startTime = Instant.now();
            QuadTreeNode root = new QuadTreeNode(0, 0, image.getWidth(), image.getHeight());

            // Tambahkan frame awal
            gifGen.addFrame(image, 1000); // Delay lebih lama untuk frame awal

            new ImageProcessor().compress(root, image, threshold, minBlockSize, method);

            // Tambahkan frame hasil kompresi
            BufferedImage compressedImage = ImageUtils.reconstructImage(root, image.getWidth(), image.getHeight());
            gifGen.addFrame(compressedImage, 1000); // Delay lebih lama untuk frame akhir

            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Simpan hasil
            try {
                String absoluteOutputPath = outputFile.getAbsolutePath();
                System.out.println("\n[Debug] Mencoba menyimpan ke: " + absoluteOutputPath);

                // Pastikan direktori output ada dan bisa ditulis
                if (!outputDir.canWrite()) {
                    System.err.println(
                            "[Error] Tidak memiliki izin untuk menulis ke direktori: " + outputDir.getAbsolutePath());
                    return;
                }

                // Simpan gambar kompresi
                try {
                    ImageUtils.saveImage(compressedImage, absoluteOutputPath);
                    System.out.println("[Debug] File berhasil disimpan");
                } catch (IOException e) {
                    System.err.println("[Error] Gagal menyimpan gambar: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }

                // Verifikasi file tersimpan
                if (!outputFile.exists()) {
                    System.err.println("[Error] File tidak berhasil disimpan di " + absoluteOutputPath);
                    return;
                }

                long outputSize = outputFile.length();

                // ------ Output ------
                System.out.println("\n[output] Kompresi berhasil!\n");
                System.out.println("[output] Waktu eksekusi         : " + duration.toMillis() + " ms");
                System.out.println("[output] Ukuran gambar sebelum  : " + inputSize);
                System.out.println("[output] Ukuran gambar setelah  : " + outputSize);
                System.out.printf("[output] Persentase kompresi    : %.2f %%\n",
                        (1 - ((double) outputSize / inputSize)) * 100);
                System.out.println("[output] Kedalaman pohon        : " + root.totalDepth());
                System.out.println("[output] Banyak simpul pohon    : " + root.totalNode());
                System.out.println("\n[output] Gambar hasil kompresi:");
                System.out.println("\t " + absoluteOutputPath);

                // Tanya user apakah ingin menyimpan sebagai GIF
                System.out.print("\n[input] Maukah anda menyimpan proses kompresi sebagai GIF? (y/n)\n>> ");
                String saveGifResponse = scanner.nextLine().toLowerCase();

                // Pada bagian penyimpanan GIF:
                if (saveGifResponse.equals("y")) {
                    try {
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String gifFileName = baseName + "_process_" + timestamp + ".gif";
                        String gifFilePath = outputDirPath + File.separator + gifFileName;

                        // Verifikasi path
                        File gifFile = new File(gifFilePath);
                        File parentDir = gifFile.getParentFile();

                        if (parentDir != null && !parentDir.exists()) {
                            if (!parentDir.mkdirs()) {
                                System.err
                                        .println("[Error] Failed to create directory: " + parentDir.getAbsolutePath());
                                return;
                            }
                        }

                        // Coba buat file kosong dulu
                        if (!gifFile.createNewFile()) {
                            System.err.println(
                                    "[Error] File already exists or cannot be created: " + gifFile.getAbsolutePath());
                            return;
                        }

                        // Simpan GIF
                        gifGen.saveGIF(gifFile.getAbsolutePath());
                        System.out.println("\n[output] GIF successfully saved to:");
                        System.out.println("\t" + gifFile.getAbsolutePath());

                    } catch (IOException e) {
                        System.err.println("[Error] Failed to save GIF:");
                        e.printStackTrace();

                        // Debug info
                        System.err.println("Debug Info:");
                        System.err.println("Directory exists: " + new File(outputDirPath).exists());
                        System.err.println("Directory writable: " + new File(outputDirPath).canWrite());
                        System.err.println("Free space: " + new File(outputDirPath).getFreeSpace() + " bytes");
                    }
                }

                System.out.println("\n[======================================================================]\n");
            } catch (Exception e) {
                System.err.println("[Error] Gagal menyimpan gambar: " + e.getMessage());
                e.printStackTrace();
                return;
            }

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}