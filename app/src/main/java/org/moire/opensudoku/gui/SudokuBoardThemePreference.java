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


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.TextView;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.CellCollection;

/**
 * A {@link Preference} that allows for setting and previewing a Sudoku Board theme.
 */
public class SudokuBoardThemePreference extends ListPreference {
    /**
     * The edit text shown in the dialog.
     */
    private SudokuBoardView mBoard;

    public SudokuBoardThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SudokuBoardThemePreference(Context context) {
        this(context, null);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View sudokuPreviewView = inflater.inflate(R.layout.preference_dialog_sudoku_board_theme, null);
        prepareSudokuPreviewView(sudokuPreviewView);

        AlertDialog dialog = (AlertDialog)getDialog();
        ListView listView = dialog.getListView();
        listView.addHeaderView(sudokuPreviewView);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        // Suppress ListPreference's custom dialog builder options to allow for a fully custom view
        // for the Sudoku theme preference dialog. This allows us to retain the other common
        // functions that the ListPreference class adds without needing to use the exact list UI.
    }

    private void prepareSudokuPreviewView(View view) {
        mBoard = (SudokuBoardView) view.findViewById(R.id.sudoku_board);
        mBoard.setFocusable(false);

        CellCollection cells = CellCollection.createDebugGame();
        cells.getCell(0, 0).setValue(1);
        cells.fillInNotes();
        mBoard.setCells(cells);
    }
}