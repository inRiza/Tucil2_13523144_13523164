package src.app;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class ImageProcessor {
    public interface CompressionCallback {
        void onNodeProcessed(QuadTreeNode node, BufferedImage image, int depth, boolean isLeaf);
    }

    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method) {
        compress(node, image, threshold, minBlockSize, method, null, 0, 0.0);
    }

    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method,
            CompressionCallback callback) {
        compress(node, image, threshold, minBlockSize, method, callback, 0, 0.0);
    }

    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method,
            CompressionCallback callback, double targetCompression) {
        compress(node, image, threshold, minBlockSize, method, callback, 0, targetCompression);
    }

    private void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize, int method,
            CompressionCallback callback, int depth, double targetCompression) {

        if (targetCompression > 0) {
            // Dynamic threshold adjustment
            double currentCompression = calculateCompressionRatio(node);
            if (currentCompression < targetCompression) {
                threshold *= 0.9; // Decrease threshold to increase compression
            } else if (currentCompression > targetCompression) {
                threshold *= 1.1; // Increase threshold to decrease compression
            }
        }

        if (shouldSplit(node, image, threshold, minBlockSize, method)) {
            // Only capture frame for significant splits
            if (callback != null && (depth <= 3 || depth % 4 == 0)) {
                callback.onNodeProcessed(node, image, depth, false);
            }

            splitNode(node);

            // Process each child node
            for (QuadTreeNode child : node.children) {
                compress(child, image, threshold, minBlockSize, method, callback, depth + 1, targetCompression);
            }

        } else {
            calculateAverageColor(node, image);

            // Only capture leaf nodes at very shallow depths
            if (callback != null && depth <= 3) {
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

        // Optimize by using direct pixel access
        int[] pixels = new int[node.width * node.height];
        image.getRGB(node.x, node.y, node.width, node.height, pixels, 0, node.width);

        for (int pixel : pixels) {
            sumR += (pixel >> 16) & 0xFF;
            sumG += (pixel >> 8) & 0xFF;
            sumB += pixel & 0xFF;
            pixelCount++;
        }

        if (pixelCount > 0) {
            node.r = (int) (sumR / pixelCount);
            node.g = (int) (sumG / pixelCount);
            node.b = (int) (sumB / pixelCount);
        }
    }

    private double calculateCompressionRatio(QuadTreeNode node) {
        int totalPixels = node.width * node.height;
        int leafNodes = countLeafNodes(node);
        return (double) leafNodes / totalPixels;
    }

    private int countLeafNodes(QuadTreeNode node) {
        if (node.isLeaf()) {
            return 1;
        }
        int count = 0;
        for (QuadTreeNode child : node.children) {
            count += countLeafNodes(child);
        }
        return count;
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