package src.app;

import java.awt.image.BufferedImage;

public class ImageProcessor {
    public void compress(QuadTreeNode node, BufferedImage image,
            double threshold, int minBlockSize) {
        if (shouldSplit(node, image, threshold, minBlockSize)) {
            int halfWidth = (int) Math.ceil(node.width / 2.0);
            int halfHeight = (int) Math.ceil(node.height / 2.0);

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

    private boolean shouldSplit(QuadTreeNode node, BufferedImage image, double threshold, int minBlockSize) {
        if (node.width <= minBlockSize || node.height <= minBlockSize) {
            return false;
        }
        double error = ErrorMeasurement.Variance(node, image);
        return error > threshold;
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
}