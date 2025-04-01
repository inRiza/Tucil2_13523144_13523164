package src.app;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class ImageProcessor {
    public interface CompressionCallback {
        void onNodeProcessed(QuadTreeNode node, BufferedImage image, int depth, boolean isLeaf);
    }

    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method) {
        compress(node, image, threshold, minBlockSize, method, null, 0);
    }

    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method,
            CompressionCallback callback) {
        compress(node, image, threshold, minBlockSize, method, callback, 0);
    }

    private void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method,
            CompressionCallback callback, int depth) {

        // Panggil callback sebelum memproses node
        if (callback != null) {
            callback.onNodeProcessed(node, image, depth, false);
        }

        if (shouldSplit(node, image, threshold, minBlockSize, method)) {
            splitNode(node);

            // Panggil callback setelah split (sebelum proses child)
            if (callback != null) {
                callback.onNodeProcessed(node, image, depth, false);
            }

            // Proses setiap child node
            for (QuadTreeNode child : node.children) {
                compress(child, image, threshold, minBlockSize, method, callback, depth + 1);
            }

            // Panggil callback setelah semua child diproses
            if (callback != null) {
                callback.onNodeProcessed(node, image, depth, false);
            }
        } else {
            calculateAverageColor(node, image);

            // Panggil callback untuk leaf node
            if (callback != null) {
                callback.onNodeProcessed(node, image, depth, true);
            }
        }
    }

    private void splitNode(QuadTreeNode node) {
        if (!node.isLeaf())
            return;

        int halfWidth = (int) Math.ceil(node.width / 2.0);
        int halfHeight = (int) Math.ceil(node.height / 2.0);

        node.children = new QuadTreeNode[4];
        node.children[0] = new QuadTreeNode(node.x, node.y, halfWidth, halfHeight); // NW
        node.children[1] = new QuadTreeNode(node.x + halfWidth, node.y, node.width - halfWidth, halfHeight); // NE
        node.children[2] = new QuadTreeNode(node.x, node.y + halfHeight, halfWidth, node.height - halfHeight); // SW
        node.children[3] = new QuadTreeNode(node.x + halfWidth, node.y + halfHeight,
                node.width - halfWidth, node.height - halfHeight); // SE
    }

    private boolean shouldSplit(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method) {
        // Tidak split jika sudah mencapai ukuran minimum
        if (node.width <= minBlockSize || node.height <= minBlockSize) {
            return false;
        }

        double error = calculateError(node, image, method);
        return error > threshold;
    }

    private double calculateError(QuadTreeNode node, BufferedImage image, int method) {
        switch (method) {
            case 1:
                return ErrorMeasurement.Variance(node, image);
            case 2:
                return ErrorMeasurement.MeanAbsoluteDeviation(node, image);
            case 3:
                return ErrorMeasurement.MaxPixelDifference(node, image);
            case 4:
                return ErrorMeasurement.Entropy(node, image);
            default:
                return ErrorMeasurement.Variance(node, image);
        }
    }

    private void calculateAverageColor(QuadTreeNode node, BufferedImage image) {
        long sumR = 0, sumG = 0, sumB = 0;
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
            node.r = (int) (sumR / pixelCount);
            node.g = (int) (sumG / pixelCount);
            node.b = (int) (sumB / pixelCount);
        }
    }

    // Utility untuk mendapatkan warna berdasarkan kedalaman
    public static Color getDepthColor(int depth) {
        // Warna akan berubah berdasarkan kedalaman node
        int r = Math.min(255, 50 + depth * 40);
        int g = Math.min(255, 100 + depth * 30);
        int b = Math.min(255, 150 + depth * 20);
        return new Color(r, g, b);
    }
}