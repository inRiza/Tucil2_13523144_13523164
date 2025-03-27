package src.app; // Sesuai lokasi file di src/app/

import src.app.utils.ImageUtils;
import src.app.utils.GIFGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class Main {
    // Direktori tetap untuk input/output
    private static final String INPUT_DIR = "test/input/";
    private static final String OUTPUT_DIR = "test/output/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Program Kompresi Gambar Quadtree ===");

            // 1. Input nama file (tanpa path)
            System.out.print("\nMasukkan nama file gambar (contoh: test.jpg): ");
            String filename = scanner.nextLine();
            File inputFile = new File(INPUT_DIR + filename);

            // Validasi file exist
            if (!inputFile.exists()) {
                System.err.println("Error: File tidak ditemukan di " + inputFile.getAbsolutePath());
                return;
            }

            // 2. Input parameter kompresi
            System.out.println("\nPilih metode error:");
            System.out.println("1. Variance\n2. MAD\n3. Max Pixel Difference\n4. Entropy");
            System.out.print("Pilihan (1-4): ");
            int method = scanner.nextInt();

            System.out.print("\nThreshold error (contoh: 10.0): ");
            double threshold = scanner.nextDouble();

            System.out.print("Ukuran blok minimum (contoh: 4): ");
            int minBlockSize = scanner.nextInt();

            // 3. Proses gambar
            BufferedImage image = ImageUtils.readImage(inputFile.getAbsolutePath());

            // Buat nama output otomatis
            String outputFilename = "compressed_" + filename;
            File outputFile = new File(OUTPUT_DIR + outputFilename);

            // Proses kompresi
            QuadTreeNode root = new QuadTreeNode(0, 0, image.getWidth(), image.getHeight());
            new ImageProcessor().compress(root, image, threshold, minBlockSize);

            // Simpan hasil
            BufferedImage compressedImage = ImageUtils.reconstructImage(root, image.getWidth(), image.getHeight());
            ImageUtils.saveImage(compressedImage, outputFile.getAbsolutePath());

            System.out.println("\nKompresi berhasil!");
            System.out.println("Hasil disimpan di: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}