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
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.ThemeUtils;

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
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == listView.getCount() - 1) {
                // Show the copy-from-theme dialog...
            } else {
                ((ColorPickerPreference) getPreference(position)).onPreferenceClick(null);
            }
        });
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
        ThemeUtils.prepareSudokuPreviewView(mBoard);
        updateThemePreview();
    }

    private void updateThemePreview() {
        ThemeUtils.applyThemeToSudokuBoardViewFromContext("custom", mBoard, getContext());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateThemePreview();
    }

    private class CustomThemeListAdapter extends BaseAdapter implements ListAdapter {
        private SudokuBoardCustomThemePreferenceGroup mPreferenceGroup;
        private Preference mCopyFromExistingThemePreference;

        CustomThemeListAdapter(SudokuBoardCustomThemePreferenceGroup preferenceGroup) {
            mPreferenceGroup = preferenceGroup;
            mCopyFromExistingThemePreference = new Preference(preferenceGroup.getContext());
            mCopyFromExistingThemePreference.setTitle("Copy from existing theme...");
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
            return mPreferenceGroup.getPreferenceCount() + 1;
        }

        @Override
        public Object getItem(int position) {
            return (position == getCount() - 1) ? mCopyFromExistingThemePreference : mPreferenceGroup.getPreference(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Preference preference = ((Preference)getItem(position));

            // we pass convertView as null for the final element to make sure we don't have a color
            // preview on the final list view item that is used to copy an existing theme
            return (position == getCount() - 1) ? preference.getView(null, parent) : preference.getView(convertView, parent);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}