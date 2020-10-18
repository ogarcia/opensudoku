package org.moire.opensudoku.game;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.moire.opensudoku.game.CellCollection.SUDOKU_SIZE;

public class SudokuGenerator {
    private static String TAG = SudokuGenerator.class.getName();

    final private Random mRandom;
    private int mAttempts;
    private int mTotalAttempts;
    private int mGames;

    public SudokuGenerator()
    {
        mGames = 0;
        mAttempts = 0;
        mTotalAttempts = 0;
        mRandom = new Random();
    }

    /**
     * Generates a random game.
     * This puts some effort into creating a game with a unique solution
     * but in order to save time, only 1-level is checked as we unset cells.
     */
    public SudokuGame generate(int numberOfEmptyCells)
    {
        mAttempts=0;
        mGames++;

        SudokuGame game;
        int i;
        do {
            mAttempts++;
            game = generateSolvedGame();
            CellCollection cells = game.getCells();
            print("Testing Solution "+mAttempts+" for game "+mGames+":\n", cells);

            LinkedList<LinkedList<Integer>> heatMap = new LinkedList<>();
            for (int r = 0; r < SUDOKU_SIZE; r++) {
                List<Integer> values = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
                Collections.shuffle(values, mRandom);
                heatMap.add(new LinkedList<>(values));
            }

            // loop through and randomly unset numberOfEmptyCells
            i = 0;
            while (i < numberOfEmptyCells) {

                if (heatMap.isEmpty()) {
                    Log.d(TAG, "generate: No more possible cells - could only unset "+i+" of "+numberOfEmptyCells);
                    break;
                }

                int randomRow = mRandom.nextInt(heatMap.size());
                LinkedList<Integer> heatRow = heatMap.get(randomRow);
                int randomColumn = heatRow.removeFirst();
                if (heatRow.isEmpty())
                    heatMap.remove(heatRow);

                Log.d(TAG, "generate: Using " + randomRow + ", " + randomColumn);

                Cell cell = cells.getCell(randomRow, randomColumn);
                int value = cell.getValue();
                if (value != 0) {
                    // unset the cell and check that it only has 1 unique solution
                    // if so, move on, otherwise, attempt to unset a different cell
                    cell.setValue(0);
                    if (isUniqueSolution(cell, cells)) i++;
                    else {
                        Log.d(TAG, "generate: Cell "+randomRow+", "+randomColumn+" has multiple solution. Trying different cell.");
                        cell.setValue(value); // restore the solved value
                    }
                }
            }
        } while (i != numberOfEmptyCells);

        // success
        mTotalAttempts += mAttempts;
        game.setCells(game.getCells()); // lock in as original cells
        print("Unsolved (attempts="+mAttempts+"):\n", game.getCells());
        return game;
    }

    /**
     * Quickly generates a completely solved game
     */
    private SudokuGame generateSolvedGame()
    {
        SudokuGame game = SudokuGame.createEmptyGame();
        CellCollection cells = game.getCells();

        // Randomize the diagonal sectors and solve the whole board (creates random solution)
        randomize(cells.getCell(0, 0).getSector(), mRandom);
        randomize(cells.getCell(3, 3).getSector(), mRandom);
        randomize(cells.getCell(6, 6).getSector(), mRandom);
        game.solve(false);

        return game;
    }

    /**
     * Randomizes the values in the group.
     * Warning: this rewrites the values current present.
     */
    private void randomize(CellGroup group, Random random) {
        List<Integer> values = Arrays.asList(1,2,3,4,5,6,7,8,9);
        Collections.shuffle(values, random); // brand new values

        Cell[] cells = group.getCells();
        for (int i=0; i<SUDOKU_SIZE; i++) {
            cells[i].setValue(values.get(i));
        }
    }

//    private CellCollection randomlyUnsetCells(CellCollection cells, int numberOfEmptyCells)
//    {
//        CellCollection newCells = cells.copy();
//
//        // loop through and randomly unset numberOfEmptyCells
//        int i = 0;
//        while (i < numberOfEmptyCells) {
//            int randomRow = mRandom.nextInt(SUDOKU_SIZE);
//            int randomColumn = mRandom.nextInt(SUDOKU_SIZE);
//
//            Cell cell = newCells.getCell(randomRow, randomColumn);
//            int value = cell.getValue();
//            if (value != 0)  {
//                cell.setValue(0);
//                if (isUniqueSolution(newCells)) i++;
//                else cell.setValue(0);
//            }
//        }
//
//        return newCells;
//    }


    /**
     * Checks if every cell in the collection has a unique solution
     */
    private boolean isUniqueSolution(CellCollection cells)
    {
        print("Testing:\n", cells);

        // loop through all cells
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                // for every empty cell
                if (cell.getValue() == 0) {
                    if (!isUniqueSolution(cell, cells))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the given cell has a unique solution in the cell collection
     **/
    private boolean isUniqueSolution(Cell cell, CellCollection cells)
    {
        String subject = cell.getRowIndex()+","+cell.getColumnIndex();

        // loop through possible solutions in that cell
        List<Integer> possibleSolutions = cell.getPossibleSolutions();
        Log.d(TAG, "isUniqueSolution: "+subject+" has possible solutions "+possibleSolutions);

        boolean solutionFound = false;
        for (int value : possibleSolutions) {
            Log.d(TAG, "isUniqueSolution: Testing "+subject+" = "+value);
            cell.setValue(value); // set
            // if more than one solution is found, fail
            if (isSolvable(cells)) {
                Log.d(TAG, "isUniqueSolution: Solution!");
                if (solutionFound)
                    return false;
                solutionFound = true;
            }
            cell.setValue(0);  // restore
        }

        if (!solutionFound)
            throw new RuntimeException("No solution found for cell");

        return true;
    }

    /**
     * Checks if the cells, in their current state, are solvable
     */
    public boolean isSolvable(CellCollection cells)
    {
        if (cells.isCompleted()) return true;

        SudokuSolver solver = new SudokuSolver();
        solver.setPuzzle(cells, false);
        return !solver.solve().isEmpty();
    }


    /**
     * Get number of attempts it took to generate a game.
     * This number resets every time generate() is called.
     *
     * @return number of attempts
     */
    public int getAttempts()
    {
        return mAttempts;
    }

    /**
     * Get number of total attempts so far on the generator.
     * This number totals all attempts since the generator was created.
     *
     * @return number of total attempts
     */
    public int getTotalAttempts()
    {
        return mTotalAttempts;
    }

    public void print(String prefix, CellCollection cells)
    {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                sb.append(cell.getValue()).append("  ");
            }
            sb.append('\n');
        }
        Log.d(TAG, prefix+sb.toString());
    }

}
