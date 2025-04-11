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
            double targetCompression = getTargetCompression(scanner);

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
            processCompressionAndGenerateGIF(image, outputFile, inputSize, method, threshold, minBlockSize, scanner,
                    targetCompression);

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
        System.out.println("\t1. Variance\n\t2. MAD\n\t3. Max Pixel Difference\n\t4. Entropy\n\t5. SSIM");
        System.out.print(">> Pilihan (1-5): ");
        return scanner.nextInt();
    }

    private static double getThreshold(Scanner scanner) {
        System.out.print("\n[input] Masukkan Threshold error (contoh: 10.0) \n>> ");
        return scanner.nextDouble();
    }

    private static double getTargetCompression(Scanner scanner) {
        System.out.print("\n[input] Masukkan target persentase kompresi (0.0 - 1.0, 0 untuk nonaktif) \n>> ");
        double target = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline
        if (target < 0 || target > 1) {
            System.out.println("[Warning] Target kompresi tidak valid, menggunakan 0 (nonaktif)");
            return 0.0;
        }
        return target;
    }

    private static int getMinBlockSize(Scanner scanner) {
        System.out.print("\n[input] Masukkan Ukuran blok minimum (contoh: 4) \n>> ");
        int size = scanner.nextInt();
        scanner.nextLine(); // Consume the newline
        return size;
    }

    private static String getOutputDirectory(Scanner scanner) {
        System.out.print("\n[input] Masukkan alamat absolut folder output \n>> ");
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print(">> ");
            input = scanner.nextLine().trim();
        }
        return input;
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
                String response = scanner.nextLine().trim().toLowerCase();
                while (!response.equals("y") && !response.equals("n")) {
                    System.out.print("Masukkan y atau n: ");
                    response = scanner.nextLine().trim().toLowerCase();
                }
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
            int method, double threshold, int minBlockSize, Scanner scanner, double targetCompression)
            throws IOException {
        // Inisialisasi GIF Generator dengan frame rate lebih cepat
        System.out.println("\n[Status] Memulai proses kompresi...");
        System.out.flush();
        GIFGenerator gifGen = new GIFGenerator(50); // Frame rate lebih cepat

        // Proses kompresi
        System.out.println("[Status] Membuat frame awal...");
        System.out.flush();
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

        System.out.println("[Status] Memulai proses kompresi gambar...");
        System.out.flush();
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
                }, targetCompression);

        System.out.println("[Status] Membuat frame akhir...");
        System.out.flush();
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

        System.out.println("[Status] Menyimpan hasil kompresi...");
        System.out.flush();
        // Simpan hasil hanya sekali
        saveResults(outputFile, finalCompressed, inputSize, outputFile.length(), root, duration, scanner, gifGen);
    }

    private static void saveResults(File outputFile, BufferedImage finalCompressed, long inputSize, long outputSize,
            QuadTreeNode root, Duration duration, Scanner scanner, GIFGenerator gifGen) throws IOException {
        String absoluteOutputPath = outputFile.getAbsolutePath();
        System.out.println("\n[Status] Mencoba menyimpan ke: " + absoluteOutputPath);
        System.out.flush();

        // Pastikan direktori output ada dan bisa ditulis
        File outputDir = outputFile.getParentFile();
        if (!outputDir.canWrite()) {
            System.err.println(
                    "[Error] Tidak memiliki izin untuk menulis ke direktori: " + outputDir.getAbsolutePath());
            return;
        }

        // Simpan gambar kompresi
        try {
            System.out.println("[Status] Menyimpan gambar hasil kompresi...");
            System.out.flush();

            // Pastikan direktori output ada
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    System.err.println("[Error] Gagal membuat direktori output: " + outputDir.getAbsolutePath());
                    return;
                }
            }

            // Simpan file
            ImageUtils.saveImage(finalCompressed, absoluteOutputPath);

            // Verifikasi file tersimpan
            if (!outputFile.exists()) {
                System.err.println("[Error] File tidak berhasil disimpan di " + absoluteOutputPath);
                return;
            }

            // Verifikasi ukuran file
            long actualFileSize = outputFile.length();
            if (actualFileSize == 0) {
                System.err.println("[Error] File berhasil dibuat tapi kosong: " + absoluteOutputPath);
                return;
            }

            System.out.println("[Status] File berhasil disimpan");
            System.out.println("[Status] Ukuran file: " + actualFileSize + " bytes");
            System.out.flush();

            // Tampilkan hasil hanya sekali
            displayResults(duration, inputSize, actualFileSize, root, absoluteOutputPath);

            // Tanya user apakah ingin menyimpan sebagai GIF
            handleGIFGeneration(outputFile, scanner, gifGen);

        } catch (IOException e) {
            System.err.println("[Error] Gagal menyimpan file");
            System.err.println("Detail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void displayResults(Duration duration, long inputSize, long outputSize, QuadTreeNode root,
            String absoluteOutputPath) {
        System.out.println("\n[output] Kompresi berhasil!\n");
        System.out.println("[output] Waktu eksekusi         : " + duration.toMillis() + " ms");
        System.out.println("[output] Ukuran gambar sebelum  : " + inputSize + " bytes");
        System.out.println("[output] Ukuran gambar setelah  : " + outputSize + " bytes");
        System.out.printf("[output] Persentase kompresi    : %.2f %%\n",
                (1 - ((double) outputSize / inputSize)) * 100);
        System.out.println("[output] Kedalaman pohon        : " + root.totalDepth());
        System.out.println("[output] Banyak simpul pohon    : " + root.totalNode());
        System.out.println("\n[output] Gambar hasil kompresi:");
        System.out.println("\t " + absoluteOutputPath);
        System.out.println("\n[output] File berhasil disimpan dan dapat diakses di alamat di atas");
        System.out.flush();
    }

    private static void handleGIFGeneration(File outputFile, Scanner scanner, GIFGenerator gifGen) throws IOException {
        System.out.print("\n[input] Maukah anda menyimpan proses kompresi sebagai GIF? (y/n)\n>> ");
        System.out.flush();
        String saveGifResponse = scanner.nextLine().trim().toLowerCase();
        while (!saveGifResponse.equals("y") && !saveGifResponse.equals("n")) {
            System.out.print("Masukkan y atau n: ");
            saveGifResponse = scanner.nextLine().trim().toLowerCase();
        }

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

                System.out.println("\n[Status] Memulai proses pembuatan GIF...");
                System.out.flush();
                // Simpan GIF
                gifGen.saveGIF(gifFile.getAbsolutePath());
                System.out.println("\n[output] GIF successfully saved to:");
                System.out.println("\t" + gifFile.getAbsolutePath());
                System.out.flush();

            } catch (IOException e) {
                System.err.println("[Error] Gagal menyimpan GIF");
                System.err.println("Detail: " + e.getMessage());
            }
        }

        System.out.println("\n[======================================================================]\n");
        System.out.flush();
    }
}