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

package org.moire.opensudoku.game.command;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;

public class ClearAllNotesCommand extends AbstractCellCommand {

	private List<NoteEntry> mOldNotes = new ArrayList<NoteEntry>();


	public ClearAllNotesCommand() {
	}


	@Override
	void saveState(Bundle outState) {
		super.saveState(outState);

		int[] rows = new int[mOldNotes.size()];
		int[] cols = new int[mOldNotes.size()];
		String[] notes = new String[mOldNotes.size()];

		int i = 0;
		for (NoteEntry ne : mOldNotes) {
			rows[i] = ne.rowIndex;
			cols[i] = ne.colIndex;
			notes[i] = ne.note.serialize();
			i++;
		}

		outState.putIntArray("rows", rows);
		outState.putIntArray("cols", cols);
		outState.putStringArray("notes", notes);
	}

	@Override
	void restoreState(Bundle inState) {
		super.restoreState(inState);

		int[] rows = inState.getIntArray("rows");
		int[] cols = inState.getIntArray("cols");
		String[] notes = inState.getStringArray("notes");

		for (int i = 0; i < rows.length; i++) {
			mOldNotes.add(new NoteEntry(rows[i], cols[i], CellNote
					.deserialize(notes[i])));
		}
	}

	@Override
	void execute() {
		CellCollection cells = getCells();

		mOldNotes.clear();
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = cells.getCell(r, c);
				CellNote note = cell.getNote();
				if (!note.isEmpty()) {
					mOldNotes.add(new NoteEntry(r, c, note));
					cell.setNote(new CellNote());
				}
			}
		}
	}

	@Override
	void undo() {
		CellCollection cells = getCells();

		for (NoteEntry ne : mOldNotes) {
			cells.getCell(ne.rowIndex, ne.colIndex).setNote(ne.note);
		}

	}

	private static class NoteEntry {
		public int rowIndex;
		public int colIndex;
		public CellNote note;

		public NoteEntry(int rowIndex, int colIndex, CellNote note) {
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
			this.note = note;
		}

	}


}
