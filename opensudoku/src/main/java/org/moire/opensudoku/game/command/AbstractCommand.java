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

/**
 * Generic interface for command in application.
 *
 * @author romario
 */
public abstract class AbstractCommand {

	public static AbstractCommand newInstance(String commandClass) {
		if (commandClass.equals(ClearAllNotesCommand.class.getSimpleName())) {
			return new ClearAllNotesCommand();
		} else if (commandClass.equals(EditCellNoteCommand.class.getSimpleName())) {
			return new EditCellNoteCommand();
		} else if (commandClass.equals(FillInNotesCommand.class.getSimpleName())) {
			return new FillInNotesCommand();
		} else if (commandClass.equals(SetCellValueCommand.class.getSimpleName())) {
			return new SetCellValueCommand();
		} else {
			throw new IllegalArgumentException(String.format("Unknown command class '%s'.", commandClass));
		}
	}

	private boolean mIsCheckpoint;

	void saveState(Bundle outState) {
		outState.putBoolean("isCheckpoint", mIsCheckpoint);
	}

	void restoreState(Bundle inState) {
		mIsCheckpoint = inState.getBoolean("isCheckpoint");
	}

	public boolean isCheckpoint() {
		return mIsCheckpoint;
	}

	public void setCheckpoint(boolean isCheckpoint) {
		mIsCheckpoint = isCheckpoint;
	}

	public String getCommandClass() {
		return getClass().getSimpleName();
	}

	/**
	 * Executes the command.
	 */
	abstract void execute();

	/**
	 * Undo this command.
	 */
	abstract void undo();

}
