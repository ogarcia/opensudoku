package org.moire.opensudoku.gui;

import android.app.ListActivity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.ThemeUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ThemedActivity extends AppCompatActivity {
    private int mThemeId = 0;
    private long mTimestampWhenApplyingTheme = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        mTimestampWhenApplyingTheme = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    }
}