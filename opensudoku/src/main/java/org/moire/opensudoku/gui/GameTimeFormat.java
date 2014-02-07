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

package org.moire.opensudoku.gui;

import java.util.Formatter;

/**
 * Game time formatter.
 *
 * @author romario
 */
public class GameTimeFormat {
	private static final int TIME_99_99 = 99 * 99 * 1000;

	private StringBuilder mTimeText = new StringBuilder();
	;
	private Formatter mGameTimeFormatter = new Formatter(mTimeText);

	/**
	 * Formats time to format of mm:ss, hours are
	 * never displayed, only total number of minutes.
	 *
	 * @param time Time in milliseconds.
	 * @return
	 */
	public String format(long time) {
		mTimeText.setLength(0);
		if (time > TIME_99_99) {
			mGameTimeFormatter.format("%d:%02d", time / 60000, time / 1000 % 60);
		} else {
			mGameTimeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		}
		return mTimeText.toString();
	}

}
