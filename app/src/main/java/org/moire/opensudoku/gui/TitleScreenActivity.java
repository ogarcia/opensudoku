package org.moire.opensudoku.gui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.utils.AndroidUtils;

public class TitleScreenActivity extends ThemedActivity {

    private Button mResumeButton;
    private Button mSudokuListButton;
    private Button mSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        mResumeButton = findViewById(R.id.resume_button);
        mSudokuListButton = findViewById(R.id.sudoku_lists_button);
        mSettingsButton = findViewById(R.id.settings_button);

        setupResumeButton();

        mSudokuListButton.setOnClickListener((view) -> {
            startActivity(new Intent(this, FolderListActivity.class));
        });

        mSettingsButton.setOnClickListener((view) -> {
            startActivity(new Intent(this, GameSettingsActivity.class));
        });

        // show changelog on first run
        Changelog changelog = new Changelog(this);
        changelog.showOnFirstRun();
    }

    private void setupResumeButton() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (gameSettings.contains("most_recently_played_sudoku_id")) {
            mResumeButton.setVisibility(View.VISIBLE);
            mResumeButton.setOnClickListener((view) -> {
                Intent intentToPlay = new Intent(TitleScreenActivity.this, SudokuPlayActivity.class);
                intentToPlay.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, gameSettings.getLong("most_recently_played_sudoku_id", 0));
                startActivity(intentToPlay);
            });
        } else {
            mResumeButton.setVisibility(View.GONE);
        }
    }

    private final int MENU_ITEM_SETTINGS = 0;
    private final int MENU_ITEM_ABOUT = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_ITEM_SETTINGS, 0, R.string.settings)
                .setShortcut('0', 's')
                .setIcon(R.drawable.ic_settings);

        menu.add(0, MENU_ITEM_ABOUT, 1, R.string.about)
                .setShortcut('1', 'h')
                .setIcon(R.drawable.ic_info);

        return true;
    }

    private final int DIALOG_ABOUT = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_SETTINGS:
                startActivity(new Intent(this, GameSettingsActivity.class));
                return true;

            case MENU_ITEM_ABOUT:
                showDialog(DIALOG_ABOUT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);

        switch (id) {
            case DIALOG_ABOUT:
                final View aboutView = factory.inflate(R.layout.about, null);
                TextView versionLabel = aboutView.findViewById(R.id.version_label);
                String versionName = AndroidUtils.getAppVersionName(getApplicationContext());
                versionLabel.setText(getString(R.string.version, versionName));
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setView(aboutView)
                        .setPositiveButton("OK", null)
                        .create();
        }

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupResumeButton();
    }
}
