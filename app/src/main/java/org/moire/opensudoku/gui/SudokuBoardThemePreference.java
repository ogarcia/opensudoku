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
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.ThemeUtils;

/**
 * A {@link Preference} that allows for setting and previewing a Sudoku Board theme.
 */
public class SudokuBoardThemePreference extends ListPreference {
    /**
     * The edit text shown in the dialog.
     */
    private SudokuBoardView mBoard;
    private int mClickedDialogEntryIndex;

    public SudokuBoardThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SudokuBoardThemePreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        String selectedTheme = getValue();
        if (selectedTheme.equals("custom_light")) {
            selectedTheme = "custom";
        }
        mClickedDialogEntryIndex = findIndexOfValue(selectedTheme);
        builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex,
                (dialog, which) -> {
                    mClickedDialogEntryIndex = which;
                    SudokuBoardThemePreference.this.applyThemePreview(
                            getEntryValues()[mClickedDialogEntryIndex].toString());
                });

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View sudokuPreviewView = inflater.inflate(R.layout.preference_dialog_sudoku_board_theme, null);
        prepareSudokuPreviewView(sudokuPreviewView, getValue());
        builder.setCustomTitle(sudokuPreviewView);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (value.equals("custom")) {
                SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getContext());
                if (gameSettings.getBoolean("custom_theme_isLightTheme", false)) {
                    value = "custom_light";
                }
            }
            if (callChangeListener(value)) {
                setValue(value);
                ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
            }
        }
    }

    private void prepareSudokuPreviewView(View view, String initialTheme) {
        mBoard = view.findViewById(R.id.sudoku_board);
        mBoard.setOnCellSelectedListener((cell) -> {
            if (cell != null) {
                mBoard.setHighlightedValue(cell.getValue());
            } else {
                mBoard.setHighlightedValue(0);
            }
        });
        ThemeUtils.prepareSudokuPreviewView(mBoard);
        applyThemePreview(initialTheme);
    }

    private void applyThemePreview(String theme) {
        ThemeUtils.applyThemeToSudokuBoardViewFromContext(theme, mBoard, getContext());
    }
}
