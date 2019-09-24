package org.moire.opensudoku.game;

public class Node {
    Node left, right, up, down;
    Node columnHeader;
    int rowID;
    int colID;
    int count;

    public Node() {
        left = right = up = down = null;
        rowID = colID = -1;
        count = 0;
    }
}