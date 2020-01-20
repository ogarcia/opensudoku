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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.AndroidUtils;
import org.moire.opensudoku.utils.ThemeUtils;

public class GameSettingsActivity extends PreferenceActivity {

    private PreferenceGroup mScreenCustomTheme;
    private int mThemeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        mThemeId = ThemeUtils.getThemeResourceIdFromPreferences(this);
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.game_settings);

        findPreference("show_hints").setOnPreferenceChangeListener(mShowHintsChanged);

        ListPreference mTheme = (ListPreference) findPreference("theme");
        mTheme.setOnPreferenceChangeListener(mThemeChanged);

        mScreenCustomTheme = (PreferenceGroup)findPreference("screen_custom_theme");
        enableScreenCustomTheme(mTheme.getValue());
    }

    @Override
    protected void onResume() {
        super.onResume();

        int newThemeId = ThemeUtils.getThemeResourceIdFromPreferences(this);
        if (newThemeId != mThemeId) {
            recreate();
        }
    }

    private OnPreferenceChangeListener mShowHintsChanged = (preference, newValue) -> {
        boolean newVal = (Boolean) newValue;

        HintsQueue hm = new HintsQueue(GameSettingsActivity.this);
        if (newVal) {
            hm.resetOneTimeHints();
        }
        return true;
    };

    private OnPreferenceChangeListener mThemeChanged = (preference, newValue) -> {
        // restart the activity to pick up the new app theme colors
        recreate();
        return true;
    };


    private void enableScreenCustomTheme(String themeName) {
        boolean enable = themeName.equals("custom");
        mScreenCustomTheme.setEnabled(enable);
        mScreenCustomTheme.setSummary(enable ?
                R.string.screen_custom_theme_summary :
                R.string.screen_custom_theme_summary_disabled);
    }
}
