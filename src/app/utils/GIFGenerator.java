package src.app.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class GIFGenerator {
    private List<BufferedImage> frames = new ArrayList<>();
    private List<Integer> delays = new ArrayList<>();
    private int defaultDelayMs;

    public GIFGenerator(int defaultDelayMs) {
        if (defaultDelayMs <= 0) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        this.defaultDelayMs = defaultDelayMs;
    }

    public void addFrame(BufferedImage image) {
        addFrame(image, defaultDelayMs);
    }

    public void addFrame(BufferedImage image, int customDelayMs) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        frames.add(copyImage(image));
        delays.add(Math.max(1, customDelayMs / 10)); // Minimum 1/100th second
    }

    public void saveGIF(String outputPath) throws IOException {
        if (frames.isEmpty()) {
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

            output = new FileImageOutputStream(outputFile);
            if (output == null) {
                throw new IOException("Failed to create output stream for: " + outputPath);
            }

            writer = new GifSequenceWriter(
                    output,
                    frames.get(0).getType(),
                    delays.get(0),
                    true);

            // Write first frame
            writer.writeToSequence(frames.get(0));

            // Write remaining frames
            for (int i = 1; i < frames.size(); i++) {
                writer.setDelay(delays.get(i));
                writer.writeToSequence(frames.get(i));
            }
        } finally {
            // Close resources in reverse order
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Warning: Error closing GifSequenceWriter: " + e.getMessage());
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println("Warning: Error closing ImageOutputStream: " + e.getMessage());
                }
            }
        }
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }
}