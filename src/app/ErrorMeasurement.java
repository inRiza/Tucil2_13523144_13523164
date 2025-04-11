package src.app;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ErrorMeasurement {

    // Konstanta untuk perhitungan SSIM
    private static final double C1 = Math.pow(0.01 * 255, 2);
    private static final double C2 = Math.pow(0.03 * 255, 2);

    // Bobot untuk channel RGB
    private static final double WEIGHT_R = 0.299;
    private static final double WEIGHT_G = 0.587;
    private static final double WEIGHT_B = 0.114;

    // Variance
    public static double Variance(QuadTreeNode node, BufferedImage image) {
        double sumR = 0, sumG = 0, sumB = 0;
        double sumSqR = 0, sumSqG = 0, sumSqB = 0;
        int pixelCount = node.width * node.height;

        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                sumR += r;
                sumG += g;
                sumB += b;
                sumSqR += r * r;
                sumSqG += g * g;
                sumSqB += b * b;
            }
        }

        double varianceR = (sumSqR - (sumR * sumR) / pixelCount) / pixelCount;
        double varianceG = (sumSqG - (sumG * sumG) / pixelCount) / pixelCount;
        double varianceB = (sumSqB - (sumB * sumB) / pixelCount) / pixelCount;

        return (varianceR + varianceG + varianceB) / 3;
    }

    // Mean Absolute Deviation
    public static double MeanAbsoluteDeviation(QuadTreeNode node, BufferedImage image) {
        double sumR = 0, sumG = 0, sumB = 0;
        double N = node.width * node.height;

        double[] averages = { 0, 0, 0 };
        AverageColor(node, image, averages);
        double avgR = averages[0];
        double avgG = averages[1];
        double avgB = averages[2];

        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                sumR += Math.abs(((rgb >> 16) & 0xFF) - avgR);
                sumG += Math.abs(((rgb >> 8) & 0xFF) - avgG);
                sumB += Math.abs((rgb & 0xFF) - avgB);
            }
        }
        return (sumR + sumG + sumB) / (3 * N);
    }

    // Max Pixel Difference
    public static double MaxPixelDifference(QuadTreeNode node, BufferedImage image) {
        int[][] maxmin = { { 0, 0 }, { 0, 0 }, { 0, 0 } };
        MaxMinColor(node, image, maxmin);
        int maxR = maxmin[0][0];
        int minR = maxmin[0][1];
        int maxG = maxmin[1][0];
        int minG = maxmin[1][1];
        int maxB = maxmin[2][0];
        int minB = maxmin[2][1];
        return ((maxR + maxG + maxB) - (minR + minG + minB)) / 3.0;
    }

    // Entropy
    public static double Entropy(QuadTreeNode node, BufferedImage image) {
        @SuppressWarnings("unchecked")
        HashMap<Integer, Double>[] probs = (HashMap<Integer, Double>[]) new HashMap[3];
        for (int i = 0; i < 3; i++) {
            probs[i] = new HashMap<>();
        }
        ProbabilityDistribution(node, image, probs);

        double entR = 0, entG = 0, entB = 0;
        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                entR -= (probs[0].get(r) * Math.log(probs[0].get(r)) / Math.log(2));
                entG -= (probs[1].get(g) * Math.log(probs[1].get(g)) / Math.log(2));
                entB -= (probs[2].get(b) * Math.log(probs[2].get(b)) / Math.log(2));
            }
        }
        return (entR + entG + entB) / 3.0;
    }

    // Helper Method
    private static void AverageColor(QuadTreeNode node, BufferedImage image, double[] colorsResult) {
        long sumR = 0;
        long sumG = 0;
        long sumB = 0;
        int pixelCount = 0;

        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                sumR += (rgb >> 16) & 0xFF;
                sumG += (rgb >> 8) & 0xFF;
                sumB += rgb & 0xFF;
                pixelCount++;
            }
        }
        if (pixelCount > 0) {
            colorsResult[0] = (double) (sumR / pixelCount);
            colorsResult[1] = (double) (sumG / pixelCount);
            colorsResult[2] = (double) (sumB / pixelCount);
        }
    }

    private static void MaxMinColor(QuadTreeNode node, BufferedImage image, int[][] colorsResult) {
        int maxR = 0;
        int maxG = 0;
        int maxB = 0;
        int minR = 255;
        int minG = 255;
        int minB = 255;
        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                maxR = Math.max(r, maxR);
                maxG = Math.max(g, maxG);
                minB = Math.max(b, maxB);

                minR = Math.min(r, minR);
                minG = Math.min(g, minG);
                minB = Math.min(b, minB);
            }
        }
        colorsResult[0][0] = maxR;
        colorsResult[0][1] = minR;
        colorsResult[1][0] = maxG;
        colorsResult[1][1] = minG;
        colorsResult[2][0] = maxB;
        colorsResult[2][1] = minB;
    }

    private static void ProbabilityDistribution(QuadTreeNode node, BufferedImage image,
            HashMap<Integer, Double>[] probabilities) {

        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                probabilities[0].computeIfPresent(r, (_, val) -> val++);
                probabilities[0].putIfAbsent(r, 1.0);
                probabilities[1].computeIfPresent(g, (_, val) -> val++);
                probabilities[1].putIfAbsent(g, 1.0);
                probabilities[2].computeIfPresent(b, (_, val) -> val++);
                probabilities[2].putIfAbsent(b, 1.0);
            }
        }

        for (int i = 0; i < 2; i++) {
            probabilities[i].replaceAll((_, val) -> val / (node.width * node.height));
        }
    }

    // Structural Similarity Index (SSIM)
    public static double SSIM(QuadTreeNode node, BufferedImage image) {
        double ssimR = 0, ssimG = 0, ssimB = 0;
        int validPixels = 0;

        // Hitung SSIM untuk setiap channel dalam blok
        for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
            for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Hitung SSIM untuk setiap channel
                ssimR += calculatePixelSSIM(r, r);
                ssimG += calculatePixelSSIM(g, g);
                ssimB += calculatePixelSSIM(b, b);

                validPixels++;
            }
        }

        if (validPixels == 0)
            return 0;

        // Rata-rata nilai SSIM untuk setiap channel
        ssimR /= validPixels;
        ssimG /= validPixels;
        ssimB /= validPixels;

        // Kombinasi tertimbang dari channel RGB
        return WEIGHT_R * ssimR + WEIGHT_G * ssimG + WEIGHT_B * ssimB;
    }

    private static double calculatePixelSSIM(int pixel1, int pixel2) {
        double meanX = pixel1;
        double meanY = pixel2;

        double varX = 0;
        double varY = 0;
        double covXY = 0;

        // Untuk pixel tunggal, varians dan kovarians adalah nol
        // Tetapi kita masih bisa menghitung komponen luminance dan contrast

        double luminance = (2 * meanX * meanY + C1) / (meanX * meanX + meanY * meanY + C1);
        double contrast = (2 * Math.sqrt(varX) * Math.sqrt(varY) + C2) / (varX + varY + C2);
        double structure = (covXY + C2 / 2) / (Math.sqrt(varX) * Math.sqrt(varY) + C2 / 2);

        // Untuk pixel tunggal, struktur term adalah 1
        return luminance * contrast * 1.0;
    }
}
