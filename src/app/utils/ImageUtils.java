package src.app.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import src.app.QuadTreeNode;

public class ImageUtils {
    public static BufferedImage readImage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File tidak ditemukan: " + filePath);
        }
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Format gambar tidak didukung: " + filePath);
        }
        return image;
    }

    public static void saveImage(BufferedImage image, String outputPath) throws IOException {
        String format = outputPath.substring(outputPath.lastIndexOf(".") + 1).toLowerCase();
        if (!ImageIO.write(image, format, new File(outputPath))) {
            throw new IOException("Tidak ditemukan writer untuk format: " + format);
        }
    }

    public static BufferedImage reconstructImage(QuadTreeNode root, int width, int height) {
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (root != null) {
            renderQuadTree(outputImage, root);
        }
        return outputImage;
    }

    private static void renderQuadTree(BufferedImage image, QuadTreeNode node) {
        if (node == null)
            return;

        if (node.isLeaf()) {
            fillNode(image, node);
        } else {
            for (QuadTreeNode child : node.children) {
                renderQuadTree(image, child);
            }
        }
    }

    private static void fillNode(BufferedImage image, QuadTreeNode node) {
        int rgb = (node.r << 16) | (node.g << 8) | node.b;
        int endY = Math.min(node.y + node.height, image.getHeight());
        int endX = Math.min(node.x + node.width, image.getWidth());

        for (int y = node.y; y < endY; y++) {
            for (int x = node.x; x < endX; x++) {
                image.setRGB(x, y, rgb);
            }
        }
    }

    // Method untuk visualisasi proses Quadtree
    public static BufferedImage drawQuadTree(BufferedImage original, QuadTreeNode node) {
        BufferedImage frame = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = frame.createGraphics();

        // Gambar gambar asli sebagai background
        g.drawImage(original, 0, 0, null);

        // Gambar garis pembatas quadtree
        drawTreeBoundaries(g, node, Color.RED, 1);

        g.dispose();
        return frame;
    }

    public static BufferedImage highlightNode(BufferedImage original, QuadTreeNode node, Color highlightColor) {
        BufferedImage frame = drawQuadTree(original, node);
        Graphics2D g = frame.createGraphics();

        // Highlight node yang sedang diproses
        g.setColor(new Color(
                highlightColor.getRed(),
                highlightColor.getGreen(),
                highlightColor.getBlue(),
                100)); // Transparansi 100/255

        g.fillRect(node.x, node.y, node.width, node.height);

        // Gambar border tebal
        g.setColor(highlightColor.darker());
        g.setStroke(new BasicStroke(2));
        g.drawRect(node.x, node.y, node.width - 1, node.height - 1);

        g.dispose();
        return frame;
    }

    private static void drawTreeBoundaries(Graphics2D g, QuadTreeNode node, Color color, int depth) {
        if (node == null)
            return;

        // Variasikan warna berdasarkan kedalaman
        Color depthColor = new Color(
                Math.min(255, color.getRed() + depth * 20),
                Math.min(255, color.getGreen() + depth * 10),
                Math.min(255, color.getBlue() + depth * 5));

        g.setColor(depthColor);
        g.setStroke(new BasicStroke(1));

        if (node.isLeaf()) {
            g.drawRect(node.x, node.y, node.width - 1, node.height - 1);
        } else {
            g.drawRect(node.x, node.y, node.width - 1, node.height - 1);
            for (QuadTreeNode child : node.children) {
                drawTreeBoundaries(g, child, color, depth + 1);
            }
        }
    }

    // Method untuk menggambar state quadtree
    public static void drawQuadTreeState(Graphics2D g, QuadTreeNode node, int currentDepth, int maxDepth) {
        if (node == null || currentDepth > maxDepth)
            return;

        // Set warna berdasarkan kedalaman (memastikan nilai tetap dalam range 0-255)
        int red = Math.max(0, Math.min(255, 255 - currentDepth * 30));
        int green = Math.max(0, Math.min(255, 100 + currentDepth * 20));
        int blue = Math.max(0, Math.min(255, 100 + currentDepth * 20));

        Color color = new Color(red, green, blue);

        // Gambar batas node
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
        g.drawRect(node.x, node.y, node.width - 1, node.height - 1);

        // Gambar anak secara rekursif
        if (!node.isLeaf()) {
            for (QuadTreeNode child : node.children) {
                drawQuadTreeState(g, child, currentDepth + 1, maxDepth);
            }
        }
    }

    // Utility untuk menggambar teks di gambar
    public static BufferedImage addDebugText(BufferedImage image, String text) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(text, 10, 20);

        g.dispose();
        return newImage;
    }
}