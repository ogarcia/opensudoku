package org.moire.opensudoku.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.moire.opensudoku.game.CellCollection.SUDOKU_SIZE;

public class GeneratedSudokuGame extends SudokuGame {
    private static final String TAG = GeneratedSudokuGame.class.getSimpleName();

    public static interface ProgressListener {
        public void onProgressChanged(int numEmptyCells, int totalEmptyCells);
    }

    private int mAttempts;
    private int mUntouched;
    private long mGenTime;

    final private Random mRandom;

    private ProgressListener mListener;

    /**
     * Generate a random, fully solved board
     */
    public GeneratedSudokuGame() {
        mRandom = new Random();
        mAttempts = 0;
        mUntouched = 0;
        mGenTime = 0;
        mListener = null;
    }

    public void setProgressListener(ProgressListener l) {
        mListener = l;
    }

    public int getNumAttempts() {
        return mAttempts;
    }

    public int getNumUntouched() {
        return mUntouched;
    }

    public long getGenTime(TimeUnit units) {
        return TimeUnit.MILLISECONDS.convert(mGenTime, units);
    }

    /**
     * Randomizes the values in the group.
     * Warning: this rewrites the values current present.
     */
    private void randomize(CellGroup group, Random random) {
        List<Integer> values = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Collections.shuffle(values, random); // brand new values

        Cell[] cells = group.getCells();
        for (int i = 0; i < SUDOKU_SIZE; i++) {
            cells[i].setValue(values.get(i));
        }
    }


    /**
     * Executes the game generation code. Not guaranteed to achieve numberOfEmptyCells.
     *
     * @param numberOfEmptyCells The goal number of empty cells we're trying to achieve
     * @return The actual number of empty cells while trying to reach numberOfEmptyCells.
     */
    public int generate(int numberOfEmptyCells) {
        long start = System.currentTimeMillis();

        // Start with an empty board
        setCells(CellCollection.createEmpty());
        setCreated(System.currentTimeMillis());
        CellCollection cells = getCells();

        // Randomize the diagonal sectors and solve the whole board (creates random solution)
        randomize(cells.getCell(0, 0).getSector(), mRandom);
        randomize(cells.getCell(3, 3).getSector(), mRandom);
        randomize(cells.getCell(6, 6).getSector(), mRandom);
        solve(false);

        Log.d(TAG, "Generating for solution:\n" + cells.toBoardString());

        // Randomize all the cells on the board so we can work on it randomly
        HashMap<Integer, LinkedList<Integer>> untouchedMap = new HashMap<>();
        for (int i = 0; i < SUDOKU_SIZE; i++) {
            List<Integer> values = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
            Collections.shuffle(values, mRandom);
            untouchedMap.put(i, new LinkedList<>(values));
        }

        LinkedList<Cell> untouched = new LinkedList<>();
        while (!untouchedMap.isEmpty()) {
            int randomRow = new ArrayList<>(untouchedMap.keySet())
                    .get(mRandom.nextInt(untouchedMap.size()));
            LinkedList<Integer> untouchedRow = untouchedMap.get(randomRow);
            int randomColumn = untouchedRow.removeFirst();
            if (untouchedRow.isEmpty()) untouchedMap.remove(randomRow);
            untouched.add(cells.getCell(randomRow, randomColumn));
        }


        // loop through and randomly unset numberOfEmptyCells
        LinkedList<Cell> empty = new LinkedList<>();
        LinkedList<Cell> left = new LinkedList<>();

        int stage = 0;

        do {
            if (mListener != null)
                mListener.onProgressChanged(empty.size(), numberOfEmptyCells);

            if (untouched.isEmpty()) {
                Log.d(TAG, "generate: All cells touched");
                break;
            }

            Cell cell = untouched.removeFirst();
            int value = cell.getValue();
            String subject = cell.getRowIndex() + "," + cell.getColumnIndex();

            switch (stage) {
                case 0:
                    // safe to unset 12 fields right away
                    if (empty.size() < 12) {
                        cell.setValue(0);
                        empty.add(cell);
                        Log.d(TAG, "generate: UNSET " + subject + " from " + value + " => 0 - " + empty.size() + " of first 12");
                        break;
                    }
                    // otherwise switch to stage 1
                    Log.d(TAG, "generate: switching to stage 1!");
                    stage = 1; // dont break so we dont skip this cell
                case 1:
                    // safe to unset if the cell only has 1 solution
                    // else undo and allow it to process in stage 2 (by putting it in the left list)
                    cell.setValue(0);
                    List<Integer> solutions = cell.getPossibleSolutions();
                    if (solutions.size() == 1) {
                        empty.add(cell);
                        Log.d(TAG, "generate: UNSET " + subject + " from " + value + " => 0 - since only 1 solution");
                    } else {
                        cell.setValue(value);
                        left.add(cell);
                        mAttempts++;
                        Log.d(TAG, "generate: STAGE " + subject + " from " + value + " => 0 - too many solutions: " + solutions + " attempt: " + mAttempts);
                    }

                    // advance to stage 2 if we tried all cells
                    if (untouched.isEmpty()) {
                        Log.d(TAG, "generate: switching to stage 2 with " + left.size() + " items to go!");
                        untouched = left;
                        left = new LinkedList<>();
                        stage = 2;
                    }
                    break; // we can completely break since this cell was added to the list for the next step
                case 2:
                    // ok to unset if only one of the possible solutions is valid
                    List<Integer> possible = cell.getPossibleSolutions();
                    possible.remove((Integer) value); // we know current value is valid

                    // if another one is solvable, fail this cell since more than 1 solution
                    boolean failed = false;
                    for (int pvalue : possible) {
                        cell.setValue(pvalue);
                        if (isSolvable(cells)) {
                            Log.d(TAG, "generate: Failed " + subject + " from " + value + " => 0 - too many valid solutions: " + pvalue + " and " + value);
                            cell.setValue(value);
                            left.add(cell);
                            failed = true;
                            break;
                        }
                    }

                    // otherwise, unset it
                    if (!failed) {
                        Log.d(TAG, "generate: UNSET " + subject + " from " + value + " => 0 - since only 1 valid solution");
                        cell.setValue(0);
                        empty.add(cell);
                    }

                    break; // no more stages to fall into
            }
        } while (empty.size() < numberOfEmptyCells);

        // finalize cells
        setCells(cells);

        Log.d(TAG, "Finalizing result!");

        // calculate untouched cells
        mUntouched = untouched.size();
        boolean solvable = isSolvable(cells);
        boolean unique = isUniqueSolution(cells, empty);
        boolean success = unique && solvable && numberOfEmptyCells == empty.size();
        mGenTime = System.currentTimeMillis() - start;

        Log.d(TAG, "Unsolved ("
                + "time: " + mGenTime + " ms"
                + ", empty=" + empty.size()
                + ", of=" + numberOfEmptyCells
                + ", solvable=" + solvable
                + ", unique=" + unique
                + ", successful=" + success
                + ", attempts=" + mAttempts
                + ", untouched=" + mUntouched
                + ", left=" + left.size() + "):\n"
                + getCells().toBoardString());

        return empty.size();
    }


    /**
     * If the game is unique if the cel is unset, unset the cell and return true
     */
    private static boolean isUniqueSolution(CellCollection cells, List<Cell> unsetCells) {
        for (Cell unset : unsetCells) {
            if (!isCellUniqueIfUnset(unset, cells, false)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given cell has a unique solution in the cell collection
     **/
    private static boolean isCellUniqueIfUnset(Cell cell, CellCollection cells, boolean print) {
        String subject = cell.getRowIndex() + "," + cell.getColumnIndex();
        int value = cell.getValue(); // save original value
        cell.setValue(0);

        // get possible values & make sure it makes sense
        List<Integer> possible = cell.getPossibleSolutions();
        if (value != 0 && !possible.contains(value))
            throw new RuntimeException("for " + subject + ", somehow a real value " + value + " is not possible: " + possible);

        // if only 1 possible value, its def unique
        // otherwise loop through possible solutions in that cell for other solutions
        List<Integer> valid;
        if (possible.size() == 1) valid = possible;
        else {
            valid = new ArrayList<>();
            for (int pvalue : possible) {
                // skip the real value to speed up
                if (pvalue == value) {
                    valid.add(value);
                    continue;
                }
                cell.setValue(pvalue); // set possible value
                if (isSolvable(cells)) valid.add(pvalue);
                if (valid.size() > 1) break; // stop if more than 1
            }
        }

        if (print)
            Log.d(TAG, "isCellUniqueIfUnset: " + subject + " has possible=" + possible + " valid=" + valid);
        if (valid.isEmpty()) throw new RuntimeException("No solution found for cell");

        cell.setValue(value); // restore original value
        return valid.size() == 1;
    }

    /**
     * Checks if the cells, in their current state, are solvable
     */
    public static boolean isSolvable(CellCollection cells) {
        if (cells.isCompleted()) return true;

        SudokuSolver solver = new SudokuSolver();
        solver.setPuzzle(cells, false);
        return !solver.solve().isEmpty();
    }
}
