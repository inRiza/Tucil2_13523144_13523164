package src.app;

import src.app.utils.ImageUtils;
import src.app.utils.GIFGenerator;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Bersihkan Terminal
        clearTerminal();

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("[=================[ Program Kompresi Gambar Quadtree ]=================]");

            // 1. Input nama file (tanpa path)
            String filename = getInputFilename(scanner);
            File inputFile = new File(filename);
            long inputSize = inputFile.length();

            // Validasi file exist dan path
            if (!validateInputFile(inputFile)) {
                return;
            }

            // 2. Input parameter kompresi
            int method = getCompressionMethod(scanner);
            double threshold = getThreshold(scanner);
            int minBlockSize = getMinBlockSize(scanner);

            // 3. Proses gambar
            BufferedImage image = null;
            try {
                image = ImageUtils.readImage(inputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[Error] Image path tidak terdeteksi oleh sistem");
                System.err.println("Detail: " + e.getMessage());
                return;
            }

            // Buat nama output otomatis
            String outputDirPath = getOutputDirectory(scanner);
            String outputFilename = generateOutputFilename(inputFile, outputDirPath);
            File outputFile = new File(outputFilename);

            // Validasi dan buat direktori output
            if (!validateAndCreateOutputDirectory(outputFile, scanner)) {
                return;
            }

            // Proses kompresi dan generate GIF
            processCompressionAndGenerateGIF(image, outputFile, inputSize, method, threshold, minBlockSize, scanner);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static boolean validateInputFile(File inputFile) {
        if (!inputFile.exists()) {
            System.err.println("[Error] File tidak ditemukan di " + inputFile.getAbsolutePath());
            return false;
        }

        if (!inputFile.isFile()) {
            System.err.println("[Error] Path yang diberikan bukan merupakan file: " + inputFile.getAbsolutePath());
            return false;
        }

        if (!inputFile.canRead()) {
            System.err.println("[Error] File tidak dapat dibaca: " + inputFile.getAbsolutePath());
            return false;
        }

        // Validasi ekstensi file
        String fileName = inputFile.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            System.err.println("[Error] Format file tidak didukung. Gunakan format JPG, JPEG, atau PNG");
            return false;
        }

        return true;
    }

    private static void clearTerminal() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getInputFilename(Scanner scanner) {
        System.out.print("\n[input] Masukkan nama absolute path gambar \n>> ");
        return scanner.nextLine();
    }

    private static int getCompressionMethod(Scanner scanner) {
        System.out.println("\n[input] Pilih metode error: ");
        System.out.println("\t1. Variance\n\t2. MAD\n\t3. Max Pixel Difference\n\t4. Entropy");
        System.out.print(">> Pilihan (1-4): ");
        return scanner.nextInt();
    }

    private static double getThreshold(Scanner scanner) {
        System.out.print("\n[input] Masukkan Threshold error (contoh: 10.0) \n>> ");
        return scanner.nextDouble();
    }

    private static int getMinBlockSize(Scanner scanner) {
        System.out.print("\n[input] Masukkan Ukuran blok minimum (contoh: 4) \n>> ");
        return scanner.nextInt();
    }

    private static String getOutputDirectory(Scanner scanner) {
        System.out.print("\n[input] Masukkan alamat absolut output gambar \n>> ");
        scanner.nextLine();
        return scanner.nextLine();
    }

    private static String generateOutputFilename(File inputFile, String outputDirPath) {
        String inputFileName = inputFile.getName();
        String extension = inputFileName.substring(inputFileName.lastIndexOf("."));
        String baseName = inputFileName.substring(0, inputFileName.lastIndexOf("."));

        // Cari index yang tersedia
        int index = 1;
        String outputFileName;
        File outputFile;
        do {
            outputFileName = baseName + "_compressed_" + index + extension;
            outputFile = new File(outputDirPath + File.separator + outputFileName);
            index++;
        } while (outputFile.exists());

        return outputDirPath + File.separator + outputFileName;
    }

    private static boolean validateAndCreateOutputDirectory(File outputFile, Scanner scanner) {
        File outputDir = outputFile.getParentFile();
        if (outputDir != null) {
            if (!outputDir.exists()) {
                System.out.println(
                        "Sepertinya alamat absolut untuk penyimpanan proses gambar yang diberikan belum ada, maukah saya buatkan alamat absolutnya?(y/n)");
                String response = scanner.nextLine().toLowerCase();
                if (response.equals("y")) {
                    if (!outputDir.mkdirs()) {
                        System.err.println("[Error] Gagal membuat direktori output: " + outputDir.getAbsolutePath());
                        return false;
                    }
                    System.out.println("Berhasil membuat tempat penyimpanan");
                } else {
                    System.out.println("Gagal menyimpan gambar");
                    return false;
                }
            }

            if (!outputDir.canWrite()) {
                System.err.println(
                        "[Error] Tidak memiliki izin untuk menulis ke direktori: " + outputDir.getAbsolutePath());
                return false;
            }
        } else {
            System.err.println("[Error] Path output tidak valid");
            return false;
        }
        return true;
    }

    private static void processCompressionAndGenerateGIF(BufferedImage image, File outputFile, long inputSize,
            int method, double threshold, int minBlockSize, Scanner scanner) throws IOException {
        // Inisialisasi GIF Generator dengan frame rate lebih cepat
        GIFGenerator gifGen = new GIFGenerator(50); // Frame rate lebih cepat

        // Proses kompresi
        Instant startTime = Instant.now();
        QuadTreeNode root = new QuadTreeNode(0, 0, image.getWidth(), image.getHeight());

        // Buat frame awal (single block)
        BufferedImage initialFrame = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = initialFrame.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        gifGen.addFrame(initialFrame, 200); // Kurangi delay frame awal

        // Proses kompresi dengan visualisasi
        new ImageProcessor().compress(root, image, threshold, minBlockSize, method,
                (node, img, depth, isLeaf) -> {
                    // Hanya capture frame untuk split yang signifikan dan kedalaman tertentu
                    if (depth <= 2 || (depth <= 4 && depth % 2 == 0)) {
                        // Buat frame baru yang menunjukkan state kompresi saat ini
                        BufferedImage frame = new BufferedImage(
                                img.getWidth(),
                                img.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = frame.createGraphics();

                        // Gambar state kompresi saat ini
                        BufferedImage currentState = ImageUtils.reconstructImage(root, img.getWidth(),
                                img.getHeight());
                        g.drawImage(currentState, 0, 0, null);

                        // Gambar batas quadtree
                        ImageUtils.drawQuadTreeState(g, root, 0, depth);

                        g.dispose();
                        gifGen.addFrame(frame, 50); // Kurangi delay antar frame
                        frame.flush();
                    }
                });

        // Tambah frame akhir
        BufferedImage finalFrame = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D finalG2d = finalFrame.createGraphics();

        // Gambar hasil kompresi akhir
        BufferedImage finalCompressed = ImageUtils.reconstructImage(root, image.getWidth(), image.getHeight());
        finalG2d.drawImage(finalCompressed, 0, 0, null);

        // Gambar batas quadtree akhir
        ImageUtils.drawQuadTreeState(finalG2d, root, 0, root.totalDepth());

        finalG2d.dispose();
        gifGen.addFrame(finalFrame, 200); // Kurangi delay frame akhir

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        // Simpan hasil
        saveResults(outputFile, finalCompressed, inputSize, outputFile.length(), root, duration, scanner, gifGen);
    }

    private static void saveResults(File outputFile, BufferedImage finalCompressed, long inputSize, long outputSize,
            QuadTreeNode root, Duration duration, Scanner scanner, GIFGenerator gifGen) throws IOException {
        String absoluteOutputPath = outputFile.getAbsolutePath();
        System.out.println("\n[Debug] Mencoba menyimpan ke: " + absoluteOutputPath);

        // Pastikan direktori output ada dan bisa ditulis
        File outputDir = outputFile.getParentFile();
        if (!outputDir.canWrite()) {
            System.err.println(
                    "[Error] Tidak memiliki izin untuk menulis ke direktori: " + outputDir.getAbsolutePath());
            return;
        }

        // Simpan gambar kompresi
        try {
            ImageUtils.saveImage(finalCompressed, absoluteOutputPath);
            System.out.println("[Debug] File berhasil disimpan");
        } catch (IOException e) {
            System.err.println("[Error] Image path tidak terdeteksi oleh sistem");
            System.err.println("Detail: " + e.getMessage());
            return;
        }

        // Verifikasi file tersimpan
        if (!outputFile.exists()) {
            System.err.println("[Error] File tidak berhasil disimpan di " + absoluteOutputPath);
            return;
        }

        // Tampilkan hasil
        displayResults(duration, inputSize, outputSize, root, absoluteOutputPath);

        // Tanya user apakah ingin menyimpan sebagai GIF
        handleGIFGeneration(outputFile, scanner, gifGen);
    }

    private static void displayResults(Duration duration, long inputSize, long outputSize, QuadTreeNode root,
            String absoluteOutputPath) {
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
    }

    private static void handleGIFGeneration(File outputFile, Scanner scanner, GIFGenerator gifGen) throws IOException {
        System.out.print("\n[input] Maukah anda menyimpan proses kompresi sebagai GIF? (y/n)\n>> ");
        String saveGifResponse = scanner.nextLine().toLowerCase();

        if (saveGifResponse.equals("y")) {
            try {
                // Gunakan index yang sama dengan file kompresi
                String baseName = outputFile.getName();
                String baseNameWithoutExt = baseName.substring(0, baseName.lastIndexOf("."));
                String gifFileName = baseNameWithoutExt.replace("_compressed_", "_gif_") + ".gif";
                String gifFilePath = outputFile.getParent() + File.separator + gifFileName;

                // Verifikasi path
                File gifFile = new File(gifFilePath);
                File parentDir = gifFile.getParentFile();

                if (parentDir != null && !parentDir.exists()) {
                    if (!parentDir.mkdirs()) {
                        System.err.println("[Error] Failed to create directory: " + parentDir.getAbsolutePath());
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
                System.err.println("[Error] Image path tidak terdeteksi oleh sistem");
                System.err.println("Detail: " + e.getMessage());

                // Debug info
                System.err.println("Debug Info:");
                System.err.println("Directory exists: " + new File(outputFile.getParent()).exists());
                System.err.println("Directory writable: " + new File(outputFile.getParent()).canWrite());
                System.err.println("Free space: " + new File(outputFile.getParent()).getFreeSpace() + " bytes");
            }
        }

        System.out.println("\n[======================================================================]\n");
    }
}