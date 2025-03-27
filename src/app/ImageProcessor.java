package src.app;

import java.awt.image.BufferedImage;

public class ImageProcessor {
    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize) {
        if (shouldSplit(node, image, threshold, minBlockSize)) {
            int halfWidth = node.width / 2;
            int halfHeight = node.height / 2;

            node.children[0] = new QuadTreeNode(node.x, node.y, halfWidth, halfHeight);
            node.children[1] = new QuadTreeNode(node.x + halfWidth, node.y, halfWidth, halfHeight);
            node.children[2] = new QuadTreeNode(node.x, node.y + halfHeight, halfWidth, halfHeight);
            node.children[3] = new QuadTreeNode(node.x + halfWidth, node.y + halfHeight, halfWidth, halfHeight);

            for (QuadTreeNode child : node.children) {
                compress(child, image, threshold, minBlockSize);
            }
        } else {
            calculateAverageColor(node, image);
        }
    }

    private boolean shouldSplit(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize) {
        if (node.width <= minBlockSize || node.height <= minBlockSize) {
            return false;
        }
        double error = calculateVariance(node, image);
        return error > threshold;
    }

    private double calculateVariance(QuadTreeNode node, BufferedImage image) {
        double sumR = 0, sumG = 0, sumB = 0;
        double sumSqR = 0, sumSqG = 0, sumSqB = 0;
        int pixelCount = node.width * node.height;

        for (int y = node.y; y < node.y + node.height; y++) {
            for (int x = node.x; x < node.x + node.width; x++) {
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

    private void calculateAverageColor(QuadTreeNode node, BufferedImage image) {
        long sumR = 0, sumG = 0, sumB = 0;
        int pixelCount = node.width * node.height;

        for (int y = node.y; y < node.y + node.height; y++) {
            for (int x = node.x; x < node.x + node.width; x++) {
                int rgb = image.getRGB(x, y);
                sumR += (rgb >> 16) & 0xFF;
                sumG += (rgb >> 8) & 0xFF;
                sumB += rgb & 0xFF;
            }
        }

        node.r = (int) (sumR / pixelCount);
        node.g = (int) (sumG / pixelCount);
        node.b = (int) (sumB / pixelCount);
    }
}