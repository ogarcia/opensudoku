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
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.AndroidUtils;
import org.moire.opensudoku.utils.ThemeUtils;

import java.util.Arrays;
import java.util.Vector;

/**
 * A {@link Preference} that allows for setting and previewing a custom Sudoku Board theme.
 */
public class SudokuBoardCustomThemePreferenceGroup extends PreferenceGroup implements
        PreferenceManager.OnActivityDestroyListener,
        ListView.OnItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private SudokuBoardView mBoard;
    private Dialog mDialog;
    private ListView mListView;
    private Dialog mCopyFromExistingThemeDialog;
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

        mListView = new ListView(getContext());
        mListView.setAdapter(new CustomThemeListAdapter(this));
        mListView.setOnItemClickListener(this);
        builder.setView(mListView);

        mGameSettings.registerOnSharedPreferenceChangeListener(this);

        mDialog = builder.create();
        mDialog.setOnDismissListener((dialog) -> {
            mGameSettings.unregisterOnSharedPreferenceChangeListener(this);
            mDialog = null;
            mListView = null;
            commitLightThemeOrDarkThemeChanges();
        });
        mDialog.show();
    }

    private void commitLightThemeOrDarkThemeChanges() {
        SwitchPreference preference = (SwitchPreference) getPreference(0);
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        String newTheme = preference.isChecked() ? "custom_light" : "custom";
        if (!mGameSettings.getString("theme", "default").equals(newTheme)) {
            settingsEditor.putString("theme", newTheme);
        }
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
        callChangeListener(null);
    }

    private void showCopyFromExistingThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.select_theme);
        builder.setNegativeButton(android.R.string.cancel, null);

        String[] themeNames = getContext().getResources().getStringArray(R.array.theme_names);
        String[] themeNamesWithoutCustomTheme = Arrays.copyOfRange(themeNames, 0, themeNames.length - 1);
        builder.setItems(themeNamesWithoutCustomTheme, (dialog, which) -> {
            copyFromExistingThemeIndex(which);
            mCopyFromExistingThemeDialog.dismiss();
        });

        mCopyFromExistingThemeDialog = builder.create();
        mCopyFromExistingThemeDialog.setOnDismissListener((dialog) -> {
            mCopyFromExistingThemeDialog = null;
        });
        mCopyFromExistingThemeDialog.show();
    }

    private void copyFromExistingThemeIndex(int which) {
        String theme = getContext().getResources().getStringArray(R.array.theme_codes)[which];
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(getContext(), ThemeUtils.getThemeResourceIdFromString(theme));

        ((SwitchPreference)getPreference(0)).setChecked(ThemeUtils.isLightTheme(theme));

        int[] attributes = {
                R.attr.colorPrimary,
                R.attr.colorPrimaryDark,
                R.attr.colorAccent,
                R.attr.lineColor,
                R.attr.sectorLineColor,
                R.attr.textColor,
                R.attr.textColorReadOnly,
                R.attr.textColorNote,
                R.attr.backgroundColor,
                R.attr.backgroundColorSecondary,
                R.attr.backgroundColorReadOnly,
                R.attr.backgroundColorTouched,
                R.attr.backgroundColorSelected,
                R.attr.backgroundColorHighlighted
        };

        TypedArray themeColors = themeWrapper.getTheme().obtainStyledAttributes(attributes);
        for (int i = 0; i < attributes.length; i++) {
            ((ColorPickerPreference)getPreference(i + 1)).onColorChanged(themeColors.getColor(i, Color.GRAY));
        }
    }

    public void onActivityDestroy() {   
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mCopyFromExistingThemeDialog!= null && mCopyFromExistingThemeDialog.isShowing()) {
            mCopyFromExistingThemeDialog.dismiss();
        }
    }

    private void prepareSudokuPreviewView(View view) {
        mBoard = (SudokuBoardView) view.findViewById(R.id.sudoku_board);
        ThemeUtils.prepareSudokuPreviewView(mBoard);
        updateThemePreview();
    }

    private void updateThemePreview() {
        String themeName = mGameSettings.getString("theme", "default");
        ThemeUtils.applyThemeToSudokuBoardViewFromContext(themeName, mBoard, getContext());
    }

    private void quantizeCustomAppColorPreferences() {
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        settingsEditor.putInt("custom_theme_colorPrimary", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorPrimary", Color.GRAY)));
        settingsEditor.putInt("custom_theme_colorPrimaryDark", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorPrimaryDark", Color.GRAY)));
        settingsEditor.putInt("custom_theme_colorAccent", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorAccent", Color.WHITE)));
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.contains("custom_theme_color")) {
            quantizeCustomAppColorPreferences();
        }
        updateThemePreview();
        if (mListView != null) {
            mListView.invalidateViews();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == parent.getCount() - 1) {
            showCopyFromExistingThemeDialog();
        } else if (position == 0) {
            SwitchPreference preference = (SwitchPreference) getPreference(position);
            preference.setChecked(!preference.isChecked());
        } else {
            ((ColorPickerPreference) getPreference(position)).onPreferenceClick(null);
        }
    }

    private class CustomThemeListAdapter extends BaseAdapter implements ListAdapter {
        private SudokuBoardCustomThemePreferenceGroup mPreferenceGroup;
        private Preference mCopyFromExistingThemePreference;

        CustomThemeListAdapter(SudokuBoardCustomThemePreferenceGroup preferenceGroup) {
            mPreferenceGroup = preferenceGroup;
            mCopyFromExistingThemePreference = new Preference(preferenceGroup.getContext());
            mCopyFromExistingThemePreference.setTitle(R.string.copy_from_existing_theme);
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

            // we pass convertView as null for the first and final elements to make sure we don't
            // have a color preview on the list view items that don't edit colors
            return (position == 0 || position == getCount() - 1) ? preference.getView(null, parent) : preference.getView(convertView, parent);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}