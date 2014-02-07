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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Note attached to cell. This object is immutable by design.
 *
 * @author romario
 */
public class CellNote {
	// TODO: int would be better
	private final Set<Integer> mNotedNumbers;

	public static final CellNote EMPTY = new CellNote();

	public CellNote() {
		mNotedNumbers = Collections.unmodifiableSet(new HashSet<Integer>());
	}

	private CellNote(Set<Integer> notedNumbers) {
		mNotedNumbers = Collections.unmodifiableSet(notedNumbers);
	}

	/**
	 * Creates instance from given string (string which has been
	 * created by {@link #serialize(StringBuilder)} or {@link #serialize()} method).
	 * earlier.
	 *
	 * @param note
	 */
	public static CellNote deserialize(String note) {
		// TODO: optimalization: CellNote object don't have to be created for empty note

		Set<Integer> notedNumbers = new HashSet<Integer>();
		if (note != null && !note.equals("")) {
			StringTokenizer tokenizer = new StringTokenizer(note, ",");
			while (tokenizer.hasMoreTokens()) {
				String value = tokenizer.nextToken();
				if (!value.equals("-")) {
					notedNumbers.add(Integer.parseInt(value));
				}
			}
		}

		return new CellNote(notedNumbers);
	}


	// TODO: this should be int[]

	/**
	 * Creates note instance from given <code>Integer</code> array.
	 *
	 * @param notedNums Array of integers, which should be part of note.
	 * @return New note instance.
	 */
	public static CellNote fromIntArray(Integer[] notedNums) {
		Set<Integer> notedNumbers = new HashSet<Integer>();

		for (Integer n : notedNums) {
			notedNumbers.add(n);
		}

		return new CellNote(notedNumbers);
	}


	/**
	 * Appends string representation of this object to the given <code>StringBuilder</code>.
	 * You can later recreate object from this string by calling {@link #deserialize(String)}.
	 *
	 * @param data
	 */
	public void serialize(StringBuilder data) {
		if (mNotedNumbers.size() == 0) {
			data.append("-");
		} else {
			for (Integer num : mNotedNumbers) {
				data.append(num).append(",");
			}
		}
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
	public Set<Integer> getNotedNumbers() {
		return mNotedNumbers;
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

		Set<Integer> notedNumbers = new HashSet<Integer>(getNotedNumbers());
		if (notedNumbers.contains(number)) {
			notedNumbers.remove(number);
		} else {
			notedNumbers.add(number);
		}

		return new CellNote(notedNumbers);
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

		Set<Integer> notedNumbers = new HashSet<Integer>(getNotedNumbers());
		notedNumbers.add(number);

		return new CellNote(notedNumbers);
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

		Set<Integer> notedNumbers = new HashSet<Integer>(getNotedNumbers());
		notedNumbers.remove(number);

		return new CellNote(notedNumbers);
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
		return mNotedNumbers.size() == 0;
	}

}
