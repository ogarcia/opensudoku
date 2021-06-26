/*
 * Copyright (C) 2009 Roman Masek
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.moire.opensudoku.game;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import org.moire.opensudoku.game.command.AbstractCommand;
import org.moire.opensudoku.game.command.ClearAllNotesCommand;
import org.moire.opensudoku.game.command.CommandStack;
import org.moire.opensudoku.game.command.EditCellNoteCommand;
import org.moire.opensudoku.game.command.FillInNotesCommand;
import org.moire.opensudoku.game.command.FillInNotesWithAllValuesCommand;
import org.moire.opensudoku.game.command.SetCellValueAndRemoveNotesCommand;
import org.moire.opensudoku.game.command.SetCellValueCommand;

import java.util.ArrayList;

public class SudokuGame {

    public static final int GAME_STATE_PLAYING = 0;
    public static final int GAME_STATE_NOT_STARTED = 1;
    public static final int GAME_STATE_COMPLETED = 2;

    private long mId;
    private long mCreated;
    private int mState;
    private long mTime;
    private long mLastPlayed;
    private String mNote;
    private CellCollection mCells;
    private SudokuSolver mSolver;
    private boolean mUsedSolver = false;
    private boolean mRemoveNotesOnEntry = false;

    private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
    private CommandStack mCommandStack;
    // Time when current activity has become active.
    private long mActiveFromTime = -1;

    public SudokuGame() {
        mTime = 0;
        mLastPlayed = 0;
        mCreated = 0;

        mState = GAME_STATE_NOT_STARTED;
    }

    public static SudokuGame createEmptyGame() {
        SudokuGame game = new SudokuGame();
        game.setCells(CellCollection.createEmpty());
        // set creation time
        game.setCreated(System.currentTimeMillis());
        return game;
    }

    public void saveState(Bundle outState) {
        outState.putLong("id", mId);
        outState.putString("note", mNote);
        outState.putLong("created", mCreated);
        outState.putInt("state", mState);
        outState.putLong("time", mTime);
        outState.putLong("lastPlayed", mLastPlayed);
        outState.putString("cells", mCells.serialize());
        outState.putString("command_stack", mCommandStack.serialize());
    }

    public void restoreState(Bundle inState) {
        mId = inState.getLong("id");
        mNote = inState.getString("note");
        mCreated = inState.getLong("created");
        mState = inState.getInt("state");
        mTime = inState.getLong("time");
        mLastPlayed = inState.getLong("lastPlayed");
        mCells = CellCollection.deserialize(inState.getString("cells"));
        mCommandStack = CommandStack.deserialize(inState.getString("command_stack"), mCells);

        validate();
    }


    public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
        mOnPuzzleSolvedListener = l;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public long getCreated() {
        return mCreated;
    }

    public void setCreated(long created) {
        mCreated = created;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    /**
     * Gets time of game-play in milliseconds.
     *
     * @return
     */
    public long getTime() {
        if (mActiveFromTime != -1) {
            return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
        } else {
            return mTime;
        }
    }

    /**
     * Sets time of play in milliseconds.
     *
     * @param time
     */
    public void setTime(long time) {
        mTime = time;
    }

    public long getLastPlayed() {
        return mLastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        mLastPlayed = lastPlayed;
    }

    public CellCollection getCells() {
        return mCells;
    }

    public void setCells(CellCollection cells) {
        mCells = cells;
        validate();
        mCommandStack = new CommandStack(mCells);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public CommandStack getCommandStack() {
        return mCommandStack;
    }

    public void setCommandStack(CommandStack commandStack) {
        mCommandStack = commandStack;
    }

    public void setRemoveNotesOnEntry(boolean removeNotesOnEntry) {
        mRemoveNotesOnEntry = removeNotesOnEntry;
    }

    /**
     * Sets value for the given cell. 0 means empty cell.
     *
     * @param cell
     * @param value
     */
    public void setCellValue(Cell cell, int value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        if (cell.isProtected()) {
            if ( cell.getValue() > 0 && value > 0) { return; }
        }

        if (cell.isEditable()) {
            if (mRemoveNotesOnEntry) {
                executeCommand(new SetCellValueAndRemoveNotesCommand(cell, value));
            } else {
                executeCommand(new SetCellValueCommand(cell, value));
            }

            validate();
            if (isCompleted()) {
                finish();
                if (mOnPuzzleSolvedListener != null) {
                    mOnPuzzleSolvedListener.onPuzzleSolved();
                }
            }
        }
    }

    /**
     * Sets note attached to the given cell.
     *
     * @param cell
     * @param note
     */
    public void setCellNote(Cell cell, CellNote note) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null.");
        }

        if (cell.isEditable()) {
            executeCommand(new EditCellNoteCommand(cell, note));
        }
    }

    private void executeCommand(AbstractCommand c) {
        mCommandStack.execute(c);
    }

    /**
     * Undo last command.
     */
    public void undo() {
        mCommandStack.undo();
    }

    public boolean hasSomethingToUndo() {
        return mCommandStack.hasSomethingToUndo();
    }

    public void setUndoCheckpoint() {
        mCommandStack.setCheckpoint();
    }

    public void undoToCheckpoint() {
        mCommandStack.undoToCheckpoint();
    }

    public boolean hasUndoCheckpoint() {
        return mCommandStack.hasCheckpoint();
    }

    public void undoToBeforeMistake() {
        mCommandStack.undoToSolvableState();
    }

    @Nullable
    public Cell getLastChangedCell() {
        return mCommandStack.getLastChangedCell();
    }

    /**
     * Start game-play.
     */
    public void start() {
        mState = GAME_STATE_PLAYING;
        resume();
    }

    public void resume() {
        // reset time we have spent playing so far, so time when activity was not active
        // will not be part of the game play time
        mActiveFromTime = SystemClock.uptimeMillis();
    }

    /**
     * Pauses game-play (for example if activity pauses).
     */
    public void pause() {
        // save time we have spent playing so far - it will be reseted after resuming
        mTime += SystemClock.uptimeMillis() - mActiveFromTime;
        mActiveFromTime = -1;

        setLastPlayed(System.currentTimeMillis());
    }

    /**
     * Checks if a solution to the puzzle exists
     */
    public boolean isSolvable() {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        return !finalValues.isEmpty();
    }

    /**
     * Solves puzzle from original state
     */
    public void solve() {
        mUsedSolver = true;
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            Cell cell = mCells.getCell(row, col);
            this.setCellValue(cell, val);
        }
    }

    public boolean usedSolver() {
        return mUsedSolver;
    }

    /**
     * Solves puzzle and fills in correct value for selected cell
     */
    public void solveCell(Cell cell) {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();

        int row = cell.getRowIndex();
        int col = cell.getColumnIndex();
        for (int[] rowColVal : finalValues) {
            if (rowColVal[0] == row && rowColVal[1] == col) {
                int val = rowColVal[2];
                this.setCellValue(cell, val);
            }
        }
    }

    /**
     * Finishes game-play. Called when puzzle is solved.
     */
    private void finish() {
        pause();
        mState = GAME_STATE_COMPLETED;
    }

    /**
     * Resets game.
     */
    public void reset() {
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = mCells.getCell(r, c);
                if (cell.isEditable()) {
                    cell.setValue(0);
                    cell.setNote(new CellNote());
                }
            }
        }
        mCommandStack = new CommandStack(mCells);
        validate();
        setTime(0);
        setLastPlayed(0);
        mState = GAME_STATE_NOT_STARTED;
        mUsedSolver = false;
    }

    /**
     * Returns true, if puzzle is solved. In order to know the current state, you have to
     * call validate first.
     *
     * @return
     */
    public boolean isCompleted() {
        return mCells.isCompleted();
    }

    public void clearAllNotes() {
        executeCommand(new ClearAllNotesCommand());
    }

    /**
     * Fills in possible values which can be entered in each cell.
     */
    public void fillInNotes() {
        executeCommand(new FillInNotesCommand());
    }

    /**
     * Fills in all values which can be entered in each cell.
     */
    public void fillInNotesWithAllValues() { executeCommand(new FillInNotesWithAllValuesCommand()); }

    public void validate() {
        mCells.validate();
    }

    public interface OnPuzzleSolvedListener {
        /**
         * Occurs when puzzle is solved.
         *
         * @return
         */
        void onPuzzleSolved();
    }
}
