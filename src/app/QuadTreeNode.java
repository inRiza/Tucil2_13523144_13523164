package src.app;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

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

    public int totalNode() {
        if (this.isLeaf()) {
            return 1;
        } else {
            int totalNode = 1;
            for (int i=0; i<4; i++) {
                if (children[i] == null) {
                    continue;
                } else {
                    totalNode += children[i].totalNode();
                }
            }
            return totalNode;
        }
    }

    public int totalDepth() {
        if (this.totalNode() == 1) {
            return 1;
        } else {
            ArrayList<Integer> allDepth = new ArrayList<>();
            for (int i=0; i<4; i++) {
                if (children[i] == null) {
                    continue;
                } else {
                    allDepth.add(children[i].totalDepth());
                }
            }
            return 1 + Collections.max(allDepth);
        }
    }
}