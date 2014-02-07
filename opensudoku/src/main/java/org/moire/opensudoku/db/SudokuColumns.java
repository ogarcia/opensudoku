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

package org.moire.opensudoku.db;

import android.provider.BaseColumns;

public abstract class SudokuColumns implements BaseColumns {
	public static final String FOLDER_ID = "folder_id";
	public static final String CREATED = "created";
	public static final String STATE = "state";
	public static final String TIME = "time";
	public static final String LAST_PLAYED = "last_played";
	public static final String DATA = "data";
	public static final String PUZZLE_NOTE = "puzzle_note";
}
