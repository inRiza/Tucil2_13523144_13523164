package src.app.utils;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;

public class GifSequenceWriter {
    private ImageWriter gifWriter;
    private ImageWriteParam imageWriteParam;
    private IIOMetadata imageMetaData;
    private ImageOutputStream outputStream;
    private int delay;
    private boolean sequenceStarted = false;

    public GifSequenceWriter(
            ImageOutputStream outputStream,
            int imageType,
            int delay,
            boolean loop) throws IOException {

        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream cannot be null");
        }

        this.outputStream = outputStream;
        this.delay = delay;

        // Get GIF writer
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Available");
        }

        this.gifWriter = iter.next();
        this.imageWriteParam = gifWriter.getDefaultWriteParam();

        // Configure image metadata
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        this.imageMetaData = gifWriter.getDefaultImageMetadata(
                imageTypeSpecifier, imageWriteParam);

        configureRootMetadata(delay, loop);

        // Initialize sequence
        this.gifWriter.setOutput(outputStream);
        this.gifWriter.prepareWriteSequence(null);
        this.sequenceStarted = true;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        configureRootMetadata(delay, true);
    }

    private void configureRootMetadata(int delay, boolean loop) {
        try {
            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

            // Set delay time
            IIOMetadataNode gceNode = getNode(root, "GraphicControlExtension");
            gceNode.setAttribute("delayTime", Integer.toString(delay));
            gceNode.setAttribute("disposalMethod", "none");
            gceNode.setAttribute("userInputFlag", "FALSE");

            // Set looping
            if (loop) {
                IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
                IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
                child.setAttribute("applicationID", "NETSCAPE");
                child.setAttribute("authenticationCode", "2.0");
                child.setUserObject(new byte[] { 0x1, (byte) 0, (byte) 0 });
                appExtensionsNode.appendChild(child);
            }

            imageMetaData.setFromTree(metaFormatName, root);
        } catch (IIOInvalidTreeException e) {
            throw new RuntimeException("Failed to configure GIF metadata", e);
        }
    }

    public void writeToSequence(BufferedImage img) throws IOException {
        if (!sequenceStarted) {
            throw new IllegalStateException("Sequence not started");
        }
        if (gifWriter == null) {
            throw new IllegalStateException("Writer has been closed");
        }
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    public void close() throws IOException {
        if (sequenceStarted && gifWriter != null) {
            try {
                gifWriter.endWriteSequence();
            } finally {
                gifWriter.dispose();
                gifWriter = null;
                sequenceStarted = false;
            }
        }
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}