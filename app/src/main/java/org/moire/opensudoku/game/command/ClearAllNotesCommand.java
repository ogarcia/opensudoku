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
import java.util.StringTokenizer;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;

public class ClearAllNotesCommand extends AbstractCellCommand {

	private List<NoteEntry> mOldNotes = new ArrayList<NoteEntry>();


	public ClearAllNotesCommand() {
	}


	@Override
	public void serialize(StringBuilder data) {
		super.serialize(data);

		data.append(mOldNotes.size()).append("|");

		for (NoteEntry ne : mOldNotes) {
            data.append(ne.rowIndex).append("|");
            data.append(ne.colIndex).append("|");
			ne.note.serialize(data);
		}
	}

	@Override
	protected void _deserialize(StringTokenizer data) {
		super._deserialize(data);

        int notesSize = Integer.parseInt(data.nextToken());
		for (int i = 0; i < notesSize; i++) {
            int row = Integer.parseInt(data.nextToken());
            int col = Integer.parseInt(data.nextToken());

            mOldNotes.add(new NoteEntry(row, col, CellNote.deserialize(data.nextToken())));
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
