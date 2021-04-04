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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Collection of sudoku cells. This class in fact represents one sudoku board (9x9).
 *
 * @author romario
 */
public class CellCollection {

    public static final int SUDOKU_SIZE = 9;

    /**
     * String is expected to be in format "00002343243202...", where each number represents
     * cell value, no other information can be set using this method.
     */
    public static int DATA_VERSION_PLAIN = 0;

    /**
     * See {@link #DATA_PATTERN_VERSION_1} and {@link #serialize()}.
     * Notes stored as an array of numbers
     */
    public static int DATA_VERSION_1 = 1;

    /**
     * Notes stored as a single number.
     */
    public static int DATA_VERSION_2 = 2;

    /**
     * There was a bug in the version 2. Notes stored with an additional bar character |.
     * So, it was impossible to get the import regex matched.
     * The V2 regex was modified to allow double bar symbols
     * Bug was fixed, but version has to be changed
     */
    public static int DATA_VERSION_3 = 3;

    public static int DATA_VERSION = DATA_VERSION_3;
    private static Pattern DATA_PATTERN_VERSION_PLAIN = Pattern.compile("^\\d{81}$");
    private static Pattern DATA_PATTERN_VERSION_1 = Pattern.compile("^version: 1\\n((?#value)\\d\\|(?#note)((\\d,)+|-)\\|(?#editable)[01]\\|){0,81}$");
    private static Pattern DATA_PATTERN_VERSION_2 = Pattern.compile("^version: 2\\n((?#value)\\d\\|(?#note)(\\d){1,3}\\|{1,2}(?#editable)[01]\\|){0,81}$");
    private static Pattern DATA_PATTERN_VERSION_3 = Pattern.compile("^version: 3\\n((?#value)\\d\\|(?#note)(\\d){1,3}\\|(?#editable)[01]\\|){0,81}$");
    private final List<OnChangeListener> mChangeListeners = new ArrayList<>();
    // TODO: An array of ints is a much better than an array of Integers, but this also generalizes to the fact that two parallel arrays of ints are also a lot more efficient than an array of (int,int) objects
    // Cell's data.
    private Cell[][] mCells;
    // Helper arrays, contains references to the groups of cells, which should contain unique
    // numbers.
    private CellGroup[] mSectors;
    private CellGroup[] mRows;
    private CellGroup[] mColumns;
    private boolean mOnChangeEnabled = true;

    /**
     * Wraps given array in this object.
     *
     * @param cells
     */
    private CellCollection(Cell[][] cells) {

        mCells = cells;
        initCollection();
    }

    /**
     * Creates empty sudoku.
     *
     * @return
     */
    public static CellCollection createEmpty() {
        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        for (int r = 0; r < SUDOKU_SIZE; r++) {

            for (int c = 0; c < SUDOKU_SIZE; c++) {
                cells[r][c] = new Cell();
            }
        }

        return new CellCollection(cells);
    }

    /**
     * Generates debug game.
     *
     * @return
     */
    public static CellCollection createDebugGame() {
        CellCollection debugGame = new CellCollection(new Cell[][]{
                {new Cell(), new Cell(), new Cell(), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9),},
                {new Cell(), new Cell(), new Cell(), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3),},
                {new Cell(), new Cell(), new Cell(), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6),},
                {new Cell(2), new Cell(3), new Cell(4), new Cell(), new Cell(), new Cell(), new Cell(8), new Cell(9), new Cell(1),},
                {new Cell(5), new Cell(6), new Cell(7), new Cell(), new Cell(), new Cell(), new Cell(2), new Cell(3), new Cell(4),},
                {new Cell(8), new Cell(9), new Cell(1), new Cell(), new Cell(), new Cell(), new Cell(5), new Cell(6), new Cell(7),},
                {new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2),},
                {new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5),},
                {new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8),},
        });
        debugGame.markFilledCellsAsNotEditable();
        return debugGame;
    }

    /**
     * Creates instance from given <code>StringTokenizer</code>.
     *
     * @param data
     * @return
     */
    public static CellCollection deserialize(StringTokenizer data, int version) {
        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        int r = 0, c = 0;
        while (data.hasMoreTokens() && r < 9) {
            cells[r][c] = Cell.deserialize(data, version);
            c++;

            if (c == 9) {
                r++;
                c = 0;
            }
        }

        return new CellCollection(cells);
    }

    /**
     * Creates instance from given string (string which has been
     * created by {@link #serialize(StringBuilder)} or {@link #serialize()} method).
     * earlier.
     *
     * @param note
     */
    public static CellCollection deserialize(String data) {
        // TODO: use DATA_PATTERN_VERSION_1 to validate and extract puzzle data
        String[] lines = data.split("\n");
        if (lines.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize Sudoku, data corrupted.");
        }

        String line = lines[0];
        if (line.startsWith("version:")) {
            String[] kv = line.split(":");
            int version = Integer.parseInt(kv[1].trim());
            StringTokenizer st = new StringTokenizer(lines[1], "|");
            return deserialize(st, version);
        } else {
            return fromString(data);
        }
    }

    /**
     * Creates collection instance from given string. String is expected
     * to be in format "00002343243202...", where each number represents
     * cell value, no other information can be set using this method.
     *
     * @param data
     * @return
     */
    public static CellCollection fromString(String data) {
        // TODO: validate

        Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

        int pos = 0;
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                int value = 0;
                while (pos < data.length()) {
                    pos++;
                    if (data.charAt(pos - 1) >= '0'
                            && data.charAt(pos - 1) <= '9') {
                        // value=Integer.parseInt(data.substring(pos-1, pos));
                        value = data.charAt(pos - 1) - '0';
                        break;
                    }
                }
                Cell cell = new Cell();
                cell.setValue(value);
                cell.setEditable(value == 0);
                cells[r][c] = cell;
            }
        }

        return new CellCollection(cells);
    }

    /**
     * Returns true, if given <code>data</code> conform to format of given data version.
     *
     * @param data
     * @param dataVersion
     * @return
     */
    public static boolean isValid(String data, int dataVersion) {
        if (dataVersion == DATA_VERSION_PLAIN) {
            return DATA_PATTERN_VERSION_PLAIN.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_1) {
            return DATA_PATTERN_VERSION_1.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_2) {
            return DATA_PATTERN_VERSION_2.matcher(data).matches();
        } else if (dataVersion == DATA_VERSION_3) {
            return DATA_PATTERN_VERSION_3.matcher(data).matches();
        } else {
            throw new IllegalArgumentException("Unknown version: " + dataVersion);
        }
    }

    /**
     * Returns true, if given <code>data</code> conform to format of any version.
     *
     * @param data
     * @return
     */
    public static boolean isValid(String data) {
        return (DATA_PATTERN_VERSION_PLAIN.matcher(data).matches() ||
                DATA_PATTERN_VERSION_1.matcher(data).matches() ||
                DATA_PATTERN_VERSION_2.matcher(data).matches() ||
                DATA_PATTERN_VERSION_3.matcher(data).matches()
        );
    }

    /**
     * Return true, if no value is entered in any of cells.
     *
     * @return
     */
    public boolean isEmpty() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() != 0)
                    return false;
            }
        }
        return true;
    }

    public Cell[][] getCells() {
        return mCells;
    }

    /**
     * Gets cell at given position.
     *
     * @param rowIndex
     * @param colIndex
     * @return
     */
    public Cell getCell(int rowIndex, int colIndex) {
        return mCells[rowIndex][colIndex];
    }

    public Cell findFirstCell(int val) {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() == val)
                    return cell;
            }
        }
        return null;
    }

    public void markAllCellsAsValid() {
        mOnChangeEnabled = false;
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                mCells[r][c].setValid(true);
            }
        }
        mOnChangeEnabled = true;
        onChange();
    }

    /**
     * Validates numbers in collection according to the sudoku rules. Cells with invalid
     * values are marked - you can use getInvalid method of cell to find out whether cell
     * contains valid value.
     *
     * @return True if validation is successful.
     */
    public boolean validate() {

        boolean valid = true;

        // first set all cells as valid
        markAllCellsAsValid();

        mOnChangeEnabled = false;
        // run validation in groups
        for (CellGroup row : mRows) {
            if (!row.validate()) {
                valid = false;
            }
        }
        for (CellGroup column : mColumns) {
            if (!column.validate()) {
                valid = false;
            }
        }
        for (CellGroup sector : mSectors) {
            if (!sector.validate()) {
                valid = false;
            }
        }

        mOnChangeEnabled = true;
        onChange();

        return valid;
    }

    public boolean isCompleted() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                if (cell.getValue() == 0 || !cell.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Marks all cells as editable.
     */
    public void markAllCellsAsEditable() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.setEditable(true);
            }
        }
    }

    /**
     * Marks all filled cells (cells with value other than 0) as not editable.
     */
    public void markFilledCellsAsNotEditable() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.setEditable(cell.getValue() == 0);
            }
        }
    }

    /**
     * Fills in all valid notes for all cells based on the values in each row, column, and sector.
     * This is a destructive operation in that the existing notes are overwritten.
     */
    public void fillInNotes() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = getCell(r, c);
                cell.setNote(new CellNote());

                CellGroup row = cell.getRow();
                CellGroup column = cell.getColumn();
                CellGroup sector = cell.getSector();
                for (int i = 1; i <= SUDOKU_SIZE; i++) {
                    if (row.DoesntContain(i) && column.DoesntContain(i) && sector.DoesntContain(i)) {
                        cell.setNote(cell.getNote().addNumber(i));
                    }
                }
            }
        }
    }

    /**
     * Fills in notes with all values for all cells.
     * This is a destructive operation in that the existing notes are overwritten.
     */
    public void fillInNotesWithAllValues() {
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = getCell(r, c);
                cell.setNote(new CellNote());
                for (int i = 1; i <= SUDOKU_SIZE; i++) {
                    cell.setNote(cell.getNote().addNumber(i));
                }
            }
        }
    }

    public void removeNotesForChangedCell(Cell cell, int number) {
        if (number < 1 || number > 9) {
            return;
        }

        CellGroup row = cell.getRow();
        CellGroup column = cell.getColumn();
        CellGroup sector = cell.getSector();
        for (int i = 0; i < SUDOKU_SIZE; i++) {
            row.getCells()[i].setNote(row.getCells()[i].getNote().removeNumber(number));
            column.getCells()[i].setNote(column.getCells()[i].getNote().removeNumber(number));
            sector.getCells()[i].setNote(sector.getCells()[i].getNote().removeNumber(number));
        }
    }

    /**
     * Returns how many times each value is used in <code>CellCollection</code>.
     * Returns map with entry for each value.
     *
     * @return
     */
    public Map<Integer, Integer> getValuesUseCount() {
        Map<Integer, Integer> valuesUseCount = new HashMap<>();
        for (int value = 1; value <= CellCollection.SUDOKU_SIZE; value++) {
            valuesUseCount.put(value, 0);
        }

        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                int value = getCell(r, c).getValue();
                if (value != 0) {
                    valuesUseCount.put(value, valuesUseCount.get(value) + 1);
                }
            }
        }

        return valuesUseCount;
    }

    /**
     * Initializes collection, initialization has two steps:
     * 1) Groups of cells which must contain unique numbers are created.
     * 2) Row and column index for each cell is set.
     */
    private void initCollection() {
        mRows = new CellGroup[SUDOKU_SIZE];
        mColumns = new CellGroup[SUDOKU_SIZE];
        mSectors = new CellGroup[SUDOKU_SIZE];

        for (int i = 0; i < SUDOKU_SIZE; i++) {
            mRows[i] = new CellGroup();
            mColumns[i] = new CellGroup();
            mSectors[i] = new CellGroup();
        }

        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];

                cell.initCollection(this, r, c,
                        mSectors[((c / 3) * 3) + (r / 3)],
                        mRows[c],
                        mColumns[r]
                );
            }
        }
    }

    /**
     * Returns a string representation of this collection in a default
     * ({@link #DATA_PATTERN_VERSION_3}) format version.
     *
     * @see #serialize(StringBuilder, int)
     *
     * @return A string representation of this collection.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb, DATA_VERSION);
        return sb.toString();
    }

    /**
     * Returns a string representation of this collection in a given data format version.
     *
     * @see #serialize(StringBuilder, int)
     *
     * @return A string representation of this collection.
     */
    public String serialize(int dataVersion) {
        StringBuilder sb = new StringBuilder();
        serialize(sb, dataVersion);
        return sb.toString();
    }

    /**
     * Writes collection to given <code>StringBuilder</code> in a default
     * ({@link #DATA_PATTERN_VERSION_3}) data format version.
     *
     * @see #serialize(StringBuilder, int)
     */
    public void serialize(StringBuilder data) {
        serialize(data, DATA_VERSION);
    }

    /**
     * Writes collection to given <code>StringBuilder</code> in a given data format version.
     * You can later recreate object instance by calling {@link #deserialize(String)} method.
     *
     * Supports only {@link #DATA_PATTERN_VERSION_PLAIN} and {@link #DATA_PATTERN_VERSION_3} formats.
     * All the other data format versions are ignored and treated as
     * {@link #DATA_PATTERN_VERSION_3} format.
     *
     * @see #DATA_PATTERN_VERSION_PLAIN
     * @see #DATA_PATTERN_VERSION_3
     *
     * @param data A <code>StringBuilder</code> where to write data.
     * @param dataVersion A version of data format.
     */
    public void serialize(StringBuilder data, int dataVersion) {
        if (dataVersion > DATA_VERSION_PLAIN) {
            data.append("version: ");
            data.append(dataVersion);
            data.append("\n");
        }
        for (int r = 0; r < SUDOKU_SIZE; r++) {
            for (int c = 0; c < SUDOKU_SIZE; c++) {
                Cell cell = mCells[r][c];
                cell.serialize(data, dataVersion);
            }
        }
    }

    public void addOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + "is already registered.");
            }
            mChangeListeners.add(listener);
        }
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener is null.");
        }
        synchronized (mChangeListeners) {
            if (!mChangeListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            mChangeListeners.remove(listener);
        }
    }

    /**
     * Returns whether change notification is enabled.
     * <p>
     * If true, change notifications are distributed to the listeners
     * registered by {@link #addOnChangeListener(OnChangeListener)}.
     *
     * @return
     */
/*
    public boolean isOnChangeEnabled() {
        return mOnChangeEnabled;
    }

    /***
     * Enables or disables change notifications, that are distributed to the listeners
     * registered by {@link #addOnChangeListener(OnChangeListener)}.
     *
     * @param onChangeEnabled
     *\/
    public void setOnChangeEnabled(boolean onChangeEnabled) {
    mOnChangeEnabled = onChangeEnabled;
    }
*/

    /**
     * Notify all registered listeners that something has changed.
     */
    protected void onChange() {
        if (mOnChangeEnabled) {
            synchronized (mChangeListeners) {
                for (OnChangeListener l : mChangeListeners) {
                    l.onChange();
                }
            }
        }
    }

    public interface OnChangeListener {
        /**
         * Called when anything in the collection changes (cell's value, note, etc.)
         */
        void onChange();
    }
}
