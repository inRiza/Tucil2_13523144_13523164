package src.app.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import src.app.*;

public class ImageUtils {
    public static BufferedImage readImage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        return ImageIO.read(file);
    }

    public static void saveImage(BufferedImage image, String outputPath) throws IOException {
        String format = outputPath.substring(outputPath.lastIndexOf(".") + 1);
        ImageIO.write(image, format, new File(outputPath));
    }

    public static BufferedImage reconstructImage(QuadTreeNode root, int width, int height) {
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (root != null) {
            renderQuadTree(outputImage, root);
        }
        return outputImage;
    }

    private static void renderQuadTree(BufferedImage image, QuadTreeNode node) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            int rgb = (node.r << 16) | (node.g << 8) | node.b;
            for (int y = node.y; y < Math.min(node.y + node.height, image.getHeight()); y++) {
                for (int x = node.x; x < Math.min(node.x + node.width, image.getWidth()); x++) {
                    image.setRGB(x, y, rgb);
                }
            }
        } else {
            for (QuadTreeNode child : node.children) {
                if (child != null) {
                    renderQuadTree(image, child);
                }
            }
        }
    }
}