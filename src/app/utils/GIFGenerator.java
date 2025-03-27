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
    private int delayMs;

    public GIFGenerator(int delayMs) {
        this.delayMs = delayMs;
    }

    public void addFrame(BufferedImage image) {
        frames.add(copyImage(image));
    }

    public void saveGIF(String outputPath) throws IOException {
        try (ImageOutputStream output = new FileImageOutputStream(new File(outputPath))) {
            GifSequenceWriter writer = new GifSequenceWriter(
                    output,
                    BufferedImage.TYPE_INT_RGB,
                    delayMs,
                    true);

            for (BufferedImage frame : frames) {
                writer.writeToSequence(frame);
            }
            writer.close();
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