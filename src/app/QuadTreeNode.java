package src.app;

import java.awt.image.BufferedImage;

public class QuadTreeNode {
    public int x, y, width, height;
    public int r, g, b;
    public QuadTreeNode[] children;

    public QuadTreeNode(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.children = new QuadTreeNode[4];
    }

    public boolean isLeaf() {
        return children[0] == null;
    }
}