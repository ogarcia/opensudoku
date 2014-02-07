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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity handles import of files with extension.
 * <p/>
 * It's sole purpose is to catch intents to view files with .opensudoku
 * extension and forward it to the ImportSudokuList activity.
 * <p/>
 * I'm doing it this way, because I don't know how to add this kind of
 * intent filtering to the ImportSudokuList activity.
 *
 * @author romario
 */
public class FileImportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent(this, ImportSudokuActivity.class);
		i.setData(getIntent().getData());
		startActivity(i);
		finish();
	}

}
