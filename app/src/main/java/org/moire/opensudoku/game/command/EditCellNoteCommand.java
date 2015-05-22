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

import android.os.Bundle;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellNote;

public class EditCellNoteCommand extends AbstractCellCommand {

	private int mCellRow;
	private int mCellColumn;
	private CellNote mNote;
	private CellNote mOldNote;

	public EditCellNoteCommand(Cell cell, CellNote note) {
		mCellRow = cell.getRowIndex();
		mCellColumn = cell.getColumnIndex();
		mNote = note;
	}

	EditCellNoteCommand() {

	}

	@Override
	void saveState(Bundle outState) {
		super.saveState(outState);

		outState.putInt("cellRow", mCellRow);
		outState.putInt("cellColumn", mCellColumn);
		outState.putString("note", mNote.serialize());
		outState.putString("oldNote", mOldNote.serialize());
	}

	@Override
	void restoreState(Bundle inState) {
		super.restoreState(inState);

		mCellRow = inState.getInt("cellRow");
		mCellColumn = inState.getInt("cellColumn");
		mNote = CellNote.deserialize(inState.getString("note"));
		mOldNote = CellNote.deserialize(inState.getString("oldNote"));
	}

	@Override
	void execute() {
		Cell cell = getCells().getCell(mCellRow, mCellColumn);
		mOldNote = cell.getNote();
		cell.setNote(mNote);
	}

	@Override
	void undo() {
		Cell cell = getCells().getCell(mCellRow, mCellColumn);
		cell.setNote(mOldNote);
	}

}
