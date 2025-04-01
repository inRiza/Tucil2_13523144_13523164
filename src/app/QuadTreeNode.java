package src.app;

import java.util.ArrayList;

public class QuadTreeNode {
    // Region properties
    public final int x, y; // Position in image (top-left corner)
    public final int width, height; // Dimensions of the region
    public int r, g, b; // Average color (if leaf node)
    public QuadTreeNode[] children; // Child nodes (NW, NE, SW, SE)
    public boolean processed; // Flag for visualization

    public QuadTreeNode(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.children = null; // Initialize as null
        this.processed = false;
    }

    /**
     * Checks if this node is a leaf node (has no children)
     */
    public boolean isLeaf() {
        return children == null;
    }

    /**
     * Splits the current node into 4 child nodes
     */
    public void split() {
        if (!isLeaf())
            return;

        int halfWidth = (int) Math.ceil(width / 2.0);
        int halfHeight = (int) Math.ceil(height / 2.0);

        children = new QuadTreeNode[4];
        children[0] = new QuadTreeNode(x, y, halfWidth, halfHeight); // NW
        children[1] = new QuadTreeNode(x + halfWidth, y, width - halfWidth, halfHeight); // NE
        children[2] = new QuadTreeNode(x, y + halfHeight, halfWidth, height - halfHeight); // SW
        children[3] = new QuadTreeNode(x + halfWidth, y + halfHeight,
                width - halfWidth, height - halfHeight); // SE
    }

    /**
     * Counts total number of nodes in the subtree
     */
    public int totalNode() {
        if (isLeaf()) {
            return 1;
        }

        int count = 1; // Count this node
        for (QuadTreeNode child : children) {
            if (child != null) {
                count += child.totalNode();
            }
        }
        return count;
    }

    /**
     * Calculates maximum depth of the tree
     */
    public int totalDepth() {
        if (isLeaf()) {
            return 1;
        }

        int maxDepth = 0;
        for (QuadTreeNode child : children) {
            if (child != null) {
                maxDepth = Math.max(maxDepth, child.totalDepth());
            }
        }
        return 1 + maxDepth;
    }

    /**
     * Gets all leaf nodes in the subtree
     */
    public ArrayList<QuadTreeNode> getLeafNodes() {
        ArrayList<QuadTreeNode> leaves = new ArrayList<>();
        collectLeaves(this, leaves);
        return leaves;
    }

    private void collectLeaves(QuadTreeNode node, ArrayList<QuadTreeNode> leaves) {
        if (node.isLeaf()) {
            leaves.add(node);
        } else {
            for (QuadTreeNode child : node.children) {
                if (child != null) {
                    collectLeaves(child, leaves);
                }
            }
        }
    }

    /**
     * Gets the bounding box coordinates [x1, y1, x2, y2]
     */
    public int[] getBounds() {
        return new int[] { x, y, x + width, y + height };
    }

    /**
     * Checks if this node contains the given point
     */
    public boolean containsPoint(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    /**
     * Marks this node and all its children as processed
     * For visualization purposes
     */
    public void markAsProcessed() {
        this.processed = true;
        if (!isLeaf()) {
            for (QuadTreeNode child : children) {
                if (child != null) {
                    child.markAsProcessed();
                }
            }
        }
    }

    /**
     * Gets the center point of the node
     */
    public int[] getCenter() {
        return new int[] { x + width / 2, y + height / 2 };
    }
}