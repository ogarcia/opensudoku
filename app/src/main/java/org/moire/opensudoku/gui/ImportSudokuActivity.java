/* 
 * Copyright (C) 2009 Roman Masek, Vit Hnilica
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
 * This activity is here to keep backward compatibility, use {@link SudokuImportActivity} instead.
 *
 * @author romario
 */
public class ImportSudokuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(getIntent());
		intent.setClass(this, SudokuImportActivity.class);
		startActivity(intent);
		finish();
	}

}
