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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Note attached to cell. This object is immutable by design.
 *
 * @author romario
 *
 * 30.10.2017
 * updated by spimanov. Sets were replaced by bitwise operations
 */
public class CellNote {

	private final short mNotedNumbers;

	public static final CellNote EMPTY = new CellNote();

	public CellNote() {
		mNotedNumbers = 0;
	}

	private CellNote(short notedNumbers) {
		mNotedNumbers = notedNumbers;
	}

	/**
	 * Creates instance from given string (string which has been
	 * created by {@link #serialize(StringBuilder)} or {@link #serialize()} method).
	 * earlier.
	 *
	 * @param note
	 */
    public static CellNote deserialize(String note) {
        return deserialize(note, CellCollection.DATA_VERSION);
    }

	public static CellNote deserialize(String note, int version) {

	    int noteValue = 0;
		if (note != null && !note.equals("") && !note.equals("-"))
        {
            if (version == CellCollection.DATA_VERSION_1) {
                StringTokenizer tokenizer = new StringTokenizer(note, ",");
                while (tokenizer.hasMoreTokens()) {
                    String value = tokenizer.nextToken();
                    if (!value.equals("-")) {
                        int number = Integer.parseInt(value);
                        noteValue |= (1 << (number - 1));
                    }
                }
            } else {
                //CellCollection.DATA_VERSION_2
                noteValue = Integer.parseInt(note);
            }
        }

        return new CellNote((short)noteValue);
	}


	/**
	 * Creates note instance from given <code>int</code> array.
	 *
	 * @param notedNums Array of integers, which should be part of note.
	 * @return New note instance.
	 */
	public static CellNote fromIntArray(Integer[] notedNums) {
		int notedNumbers = 0;

		for (Integer n : notedNums) {
			notedNumbers = (short) (notedNumbers | (1 << (n - 1)));
		}

		return new CellNote((short)notedNumbers);
	}


	/**
	 * Appends string representation of this object to the given <code>StringBuilder</code>.
	 * You can later recreate object from this string by calling {@link #deserialize(String)}.
	 *
	 * @param data
	 */
	public void serialize(StringBuilder data) {
        data.append((int)mNotedNumbers);
		data.append("|");
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		serialize(sb);
		return sb.toString();
	}

	/**
	 * Returns numbers currently noted in cell.
	 *
	 * @return
	 */
	public List<Integer> getNotedNumbers() {

        List<Integer> result = new ArrayList<>();
	    int c = 1;
	    for (int i = 0; i < 9; i++) {
	        if ((mNotedNumbers & (short)c) != 0) {
                result.add(i + 1);
            }
            c = (c << 1);
        }

		return result;
	}

	/**
	 * Toggles noted number: if number is already noted, it will be removed otherwise it will be added.
	 *
	 * @param number Number to toggle.
	 * @return New CellNote instance with changes.
	 */
	public CellNote toggleNumber(int number) {
		if (number < 1 || number > 9)
			throw new IllegalArgumentException("Number must be between 1-9.");

		return new CellNote((short) (mNotedNumbers ^ (1 << (number - 1))));
	}

	/**
	 * Adds number to the cell's note (if not present already).
	 *
	 * @param number
	 * @return
	 */
	public CellNote addNumber(int number) {
		if (number < 1 || number > 9)
			throw new IllegalArgumentException("Number must be between 1-9.");

		return new CellNote((short) (mNotedNumbers | (1 << (number - 1))));
	}

	/**
	 * Removes number from the cell's note.
	 *
	 * @param number
	 * @return
	 */
	public CellNote removeNumber(int number) {
		if (number < 1 || number > 9)
			throw new IllegalArgumentException("Number must be between 1-9.");

		return new CellNote((short) (mNotedNumbers & ~(1 << (number - 1))));
	}

	public CellNote clear() {
		return new CellNote();
	}

	/**
	 * Returns true, if note is empty.
	 *
	 * @return True if note is empty.
	 */
	public boolean isEmpty() {
		return mNotedNumbers == 0;
	}

}
