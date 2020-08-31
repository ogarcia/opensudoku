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

import android.content.Context;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SudokuListFilter {

    public boolean showStateNotStarted = true;
    public boolean showStatePlaying = true;
    public boolean showStateCompleted = true;
    private Context mContext;

    public SudokuListFilter(Context context) {
        mContext = context;
    }

    @Override
    public String toString() {
        List<String> visibleStates = new ArrayList<>();
        if (showStateNotStarted) {
            visibleStates.add(mContext.getString(R.string.not_started));
        }
        if (showStatePlaying) {
            visibleStates.add(mContext.getString(R.string.playing));
        }
        if (showStateCompleted) {
            visibleStates.add(mContext.getString(R.string.solved));
        }
        return StringUtils.join(visibleStates, ",");
    }
}
