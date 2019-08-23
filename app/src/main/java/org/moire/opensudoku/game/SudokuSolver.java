package org.moire.opensudoku.game;


public class SudokuSolver {

    private int NUM_ROWS = 9;
    private int NUM_COLS = 9;
    private int NUM_VALS = 9;
    private int NUM_CONSTRAINTS = 4;
    private int NUM_CELLS = NUM_ROWS * NUM_COLS;

    private int[][] mConstraintMatrix;
    private Node[][] mLinkedList;
    private Node mHead;

    public SudokuSolver() {
        initializeConstraintMatrix();
        initializeLinkedList();
    }

    /**
     * Modifies linked list based on the current state of the board
     */
    public void setPuzzle(CellCollection mCells) {
        Cell[][] board = mCells.getCells();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int val = board[row][col].getValue();
                if (val != 0) {
                    int matrixRow = cellToRow(row, col, val-1, true);
                    int matrixCol = 9*row + col; // calculates column of node based on cell constraint

                    Node rowNode = mLinkedList[matrixRow][matrixCol];
                    Node rightNode = rowNode;
                    do {
                        cover(rightNode);
                        rightNode = rightNode.right;
                    } while (rightNode != rowNode);
                }
            }
        }
    }

    private void initializeConstraintMatrix() {
        // add row of 1's for column headers
        mConstraintMatrix = new int[NUM_ROWS * NUM_COLS * NUM_VALS + 1][NUM_CELLS * NUM_CONSTRAINTS];
        for (int j = 0; j < mConstraintMatrix[0].length; j++) {
            mConstraintMatrix[0][j] = 1;
        }

        // calculate column where constraint will go
        int rowShift = NUM_CELLS;
        int colShift = NUM_CELLS * 2;
        int blockShift = NUM_CELLS * 3;
        int cellColumn = 0;
        int rowColumn = 0;
        int colColumn = 0;
        int blockColumn = 0;

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                for (int val = 0; val < NUM_VALS; val++) {
                    int matrixRow = cellToRow(row, col, val, true);

                    // cell constraint
                    mConstraintMatrix[matrixRow][cellColumn] = 1;

                    // row constraint
                    mConstraintMatrix[matrixRow][rowColumn + rowShift] = 1;
                    rowColumn++;

                    // col constraint
                    mConstraintMatrix[matrixRow][colColumn + colShift] = 1;
                    colColumn++;

                    // block constraint
                    mConstraintMatrix[matrixRow][blockColumn + blockShift] = 1;
                    blockColumn++;
                }
                cellColumn++;
                rowColumn -= 9;
                if (col % 3 != 2) {
                    blockColumn -= 9;
                }
            }
            rowColumn += 9;
            colColumn -= 81;
            if (row % 3 != 2) {
                blockColumn -= 27;
            }
        }
    }

    private void initializeLinkedList() {
        mLinkedList = new Node[NUM_ROWS * NUM_COLS * NUM_VALS + 1][NUM_CELLS * NUM_CONSTRAINTS];
        mHead = new Node();
        int rows = mLinkedList.length;
        int cols = mLinkedList[0].length;

        // create node for each 1 in constraint matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mConstraintMatrix[i][j] == 1) {
                    mLinkedList[i][j] = new Node();
                }
            }
        }

        // link nodes in mLinkedList
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mConstraintMatrix[i][j] == 1) {
                    int a, b;

                    // link left
                    a = i;
                    b = j;
                    do {
                        b = moveLeft(b, cols);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].left = mLinkedList[a][b];

                    // link right
                    a = i;
                    b = j;
                    do {
                        b = moveRight(b, cols);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].right = mLinkedList[a][b];

                    // link up
                    a = i;
                    b = j;
                    do {
                        a = moveUp(a, rows);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].up = mLinkedList[a][b];

                    // link up
                    a = i;
                    b = j;
                    do {
                        a = moveDown(a, rows);
                    } while (mConstraintMatrix[a][b] != 1);
                    mLinkedList[i][j].down = mLinkedList[a][b];

                    // initialize remaining node info
                    mLinkedList[i][j].columnHeader = mLinkedList[0][j];
                    mLinkedList[i][j].rowID = i;
                    mLinkedList[i][j].colID = j;
                }
            }
        }

        // link head node
        mHead.right = mLinkedList[0][0];
        mHead.left = mLinkedList[0][cols - 1];
        mLinkedList[0][0].left = mHead;
        mLinkedList[0][cols - 1].right = mHead;
    }


    /* UTILITY FUNCTIONS */

    /**
     * Converts from puzzle cell to constraint matrix
     * @param row 0-8 index
     * @param col 0-8 index
     * @param val 0-8 index (representing values 1-9)
     * @param headersInMatrix true when headers are in mConstraintMatrix, false if in separate vector
     * @return row in mConstraintMatrix corresponding to cell indices and value
     */
    private int cellToRow(int row, int col, int val, boolean headersInMatrix) {
        int matrixRow = 81*row + 9*col + val;
        if (headersInMatrix) {
            matrixRow++;
        }
        return matrixRow;
    }

    /**
     * Functions to move cyclically through matrix
     */
    private int moveLeft (int j, int numCols) {return j - 1 < 0 ? numCols - 1 : j - 1;}
    private int moveRight (int j, int numCols) {return (j + 1) % numCols;}
    private int moveUp (int i, int numRows) {return i - 1 < 0 ? numRows - 1 : i -1;}
    private int moveDown (int i, int numRows) {return (i + 1) % numRows;}

    /**
     * Unlinks node from linked list
     */
    private void cover(Node node) {
        Node colNode = node.columnHeader;
        colNode.left.right = colNode.right;
        colNode.right.left = colNode.left;

        Node rowNode;
        for (rowNode = colNode.down; rowNode != colNode; rowNode = rowNode.down) {
            Node rightNode;
            for (rightNode = rowNode.right; rightNode != rowNode; rightNode = rightNode.right) {
                rightNode.up.down = rightNode.down;
                rightNode.down.up = rightNode.up;
                rightNode.columnHeader.count--;
            }
        }
    }
}
