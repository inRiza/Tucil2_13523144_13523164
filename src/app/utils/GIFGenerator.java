package src.app.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class GIFGenerator implements AutoCloseable {
    private File tempDir;
    private List<String> tempFiles;
    private List<Integer> delays;
    private int defaultDelayMs;
    private int frameCount;

    public GIFGenerator(int defaultDelayMs) {
        if (defaultDelayMs <= 0) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        this.defaultDelayMs = defaultDelayMs;
        this.tempFiles = new ArrayList<>();
        this.delays = new ArrayList<>();
        this.frameCount = 0;

        // Create temporary directory
        try {
            this.tempDir = new File(System.getProperty("java.io.tmpdir"), "quadtree_gif_" + System.currentTimeMillis());
            if (!this.tempDir.mkdirs()) {
                throw new IOException("Failed to create temporary directory");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize GIF generator", e);
        }
    }

    public void addFrame(BufferedImage image) {
        addFrame(image, defaultDelayMs);
    }

    public void addFrame(BufferedImage image, int customDelayMs) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        try {
            // Save frame to temporary file
            String tempFileName = String.format("frame_%05d.png", frameCount++);
            File tempFile = new File(tempDir, tempFileName);
            ImageIO.write(image, "png", tempFile);
            tempFiles.add(tempFile.getAbsolutePath());
            delays.add(Math.max(1, customDelayMs / 10)); // Minimum 1/100th second
        } catch (IOException e) {
            throw new RuntimeException("Failed to save frame", e);
        }
    }

    public void saveGIF(String outputPath) throws IOException {
        if (tempFiles.isEmpty()) {
            throw new IllegalStateException("No frames to save");
        }

        File outputFile = new File(outputPath);
        ImageOutputStream output = null;
        GifSequenceWriter writer = null;

        try {
            // Create parent directories if needed
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir);
                }
            }

            // Read first frame to get image type
            BufferedImage firstFrame = ImageIO.read(new File(tempFiles.get(0)));

            output = new FileImageOutputStream(outputFile);
            writer = new GifSequenceWriter(output, firstFrame.getType(), delays.get(0), true);

            // Write first frame
            writer.writeToSequence(firstFrame);

            // Write remaining frames
            for (int i = 1; i < tempFiles.size(); i++) {
                BufferedImage frame = ImageIO.read(new File(tempFiles.get(i)));
                writer.setDelay(delays.get(i));
                writer.writeToSequence(frame);
            }
        } finally {
            // Close resources
            if (writer != null)
                writer.close();
            if (output != null)
                output.close();
        }
    }

    @Override
    public void close() {
        cleanupTempFiles();
    }

    private void cleanupTempFiles() {
        for (String tempFile : tempFiles) {
            new File(tempFile).delete();
        }
        tempDir.delete();
    }
}