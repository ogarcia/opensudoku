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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.utils.AndroidUtils;

/**
 * A {@link Preference} that allows for setting and previewing a custom Sudoku Board theme.
 */
public class SudokuBoardCustomThemePreferenceGroup extends PreferenceGroup implements
        PreferenceManager.OnActivityDestroyListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private SudokuBoardView mBoard;
    private Dialog mDialog;
    private SharedPreferences mGameSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

    public SudokuBoardCustomThemePreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.preferenceScreenStyle);
        mGameSettings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SudokuBoardCustomThemePreferenceGroup(Context context) {
        this(context, null);
    }

    @Override
    protected boolean isOnSameScreenAsChildren() {
        return false;
    }

    @Override
    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.close, null);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View sudokuPreviewView = inflater.inflate(R.layout.preference_dialog_sudoku_board_theme, null);
        prepareSudokuPreviewView(sudokuPreviewView);
        builder.setCustomTitle(sudokuPreviewView);

        ListView listView = new ListView(getContext());
        listView.setAdapter(new CustomThemeListAdapter(this));
        listView.setOnItemClickListener((parent, view, position, id) -> { ((ColorPickerPreference)getPreference(position)).onPreferenceClick(null); });
        builder.setView(listView);

        mGameSettings.registerOnSharedPreferenceChangeListener(this);

        mDialog = builder.create();
        mDialog.setOnDismissListener((dialog) -> {
            mDialog = null;
            mGameSettings.unregisterOnSharedPreferenceChangeListener(this);
        });
        mDialog.show();
    }

    public void onActivityDestroy() {   
        if (mDialog == null || !mDialog.isShowing()) {
            return;
        }
        
        mDialog.dismiss();
    }

    private void prepareSudokuPreviewView(View view) {
        mBoard = (SudokuBoardView) view.findViewById(R.id.sudoku_board);
        mBoard.setFocusable(false);

        CellCollection cells = CellCollection.createDebugGame();
        cells.getCell(0, 0).setValue(1);
        cells.fillInNotes();
        mBoard.setCells(cells);

        updateThemePreview();
    }

    private void updateThemePreview() {
        mBoard.setLineColor(mGameSettings.getInt("custom_theme_lineColor", R.color.default_lineColor));
        mBoard.setSectorLineColor(mGameSettings.getInt("custom_theme_sectorLineColor", R.color.default_sectorLineColor));
        mBoard.setTextColor(mGameSettings.getInt("custom_theme_textColor", R.color.default_textColor));
        mBoard.setTextColorReadOnly(mGameSettings.getInt("custom_theme_textColorReadOnly", R.color.default_textColorReadOnly));
        mBoard.setTextColorNote(mGameSettings.getInt("custom_theme_textColorNote", R.color.default_textColorNote));
        mBoard.setBackgroundColor(mGameSettings.getInt("custom_theme_backgroundColor", R.color.default_backgroundColor));
        mBoard.setBackgroundColorSecondary(mGameSettings.getInt("custom_theme_backgroundColorSecondary", R.color.default_backgroundColorSecondary));
        mBoard.setBackgroundColorReadOnly(mGameSettings.getInt("custom_theme_backgroundColorReadOnly", R.color.default_backgroundColorReadOnly));
        mBoard.setBackgroundColorTouched(mGameSettings.getInt("custom_theme_backgroundColorTouched", R.color.default_backgroundColorTouched));
        mBoard.setBackgroundColorSelected(mGameSettings.getInt("custom_theme_backgroundColorSelected", R.color.default_backgroundColorSelected));
        mBoard.setBackgroundColorHighlighted(mGameSettings.getInt("custom_theme_backgroundColorHighlighted", R.color.default_backgroundColorHighlighted));
        mBoard.invalidate();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateThemePreview();
    }

    private class CustomThemeListAdapter extends BaseAdapter implements ListAdapter {
        private SudokuBoardCustomThemePreferenceGroup mPreferenceGroup;

        CustomThemeListAdapter(SudokuBoardCustomThemePreferenceGroup preferenceGroup) {
            mPreferenceGroup = preferenceGroup;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            return mPreferenceGroup.getPreferenceCount();
        }

        @Override
        public Object getItem(int position) {
            return mPreferenceGroup.getPreference(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mPreferenceGroup.getPreference(position).getView(convertView, parent);
        }

        @Override
        public boolean isEmpty() {
            return mPreferenceGroup.getPreferenceCount() == 0;
        }
    }
}