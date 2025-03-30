package src.app; // Sesuai lokasi file di src/app/

import src.app.utils.ImageUtils;
import src.app.utils.GIFGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class Main {
    // Direktori tetap untuk input/output
    // private static final String INPUT_DIR = "test/input/";
    // private static final String OUTPUT_DIR = "test/output/";

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
            String outputFilename = scanner.nextLine();
            File outputFile = new File(outputFilename);
            
            // Proses kompresi
            Instant startTime = Instant.now();
            QuadTreeNode root = new QuadTreeNode(0, 0, image.getWidth(), image.getHeight());
            new ImageProcessor().compress(root, image, threshold, minBlockSize, method);
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);

            // Simpan hasil
            BufferedImage compressedImage = ImageUtils.reconstructImage(root, image.getWidth(), image.getHeight());
            ImageUtils.saveImage(compressedImage, outputFile.getAbsolutePath());
            long outputSize = outputFile.length();
            
            // ------ Output ------
            System.out.println("\n[output] Kompresi berhasil!\n");
            System.out.println("[output] Waktu eksekusi         : " + duration.toMillis() + " ms");
            System.out.println("[output] Ukuran gambar sebelum  : " + inputSize);
            System.out.println("[output] Ukuran gambar setelah  : " + outputSize);
            System.out.println("[output] Persentase kompresi    : " + (1-(inputSize/outputSize))*100 + " %");
            System.out.println("[output] Kedalaman pohon        : " + root.totalDepth());
            System.out.println("[output] Banyak simpul pohon    : " + root.totalNode());
            System.out.println("\n[output] Gambar hasil kompresi:");
            System.out.println("\t " + outputFile.getAbsolutePath());
            System.out.println("\n[======================================================================]\n");

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}