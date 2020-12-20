package org.moire.opensudoku.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;

import org.moire.opensudoku.R;
import org.moire.opensudoku.gui.importing.AbstractImportTask;
import org.moire.opensudoku.gui.importing.AbstractImportTask.OnImportFinishedListener;
import org.moire.opensudoku.gui.importing.ExtrasImportTask;
import org.moire.opensudoku.gui.importing.OpenSudokuImportTask;
import org.moire.opensudoku.gui.importing.SdmImportTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * This activity is responsible for importing puzzles from various sources
 * (web, file, .opensudoku, .sdm, extras).
 *
 * @author romario
 */
public class SudokuImportActivity extends ThemedActivity {
    /**
     * Name of folder to which games should be imported.
     */
    public static final String EXTRA_FOLDER_NAME = "FOLDER_NAME";
    /**
     * Indicates whether games should be appended to the existing folder if such
     * folder exists.
     */
    public static final String EXTRA_APPEND_TO_FOLDER = "APPEND_TO_FOLDER";
    /**
     * Games (puzzles) to import. String should be in this format:
     * 120001232...0041\n 456000213...1100\n
     */
    public static final String EXTRA_GAMES = "GAMES";

    private static final String TAG = "ImportSudokuActivity";
    private final OnImportFinishedListener mOnImportFinishedListener = (importSuccessful, folderId) -> {
        if (importSuccessful) {
            if (folderId == -1) {
                // multiple folders were imported, go to folder list
                Intent i = new Intent(SudokuImportActivity.this,
                        FolderListActivity.class);
                startActivity(i);
            } else {
                // one folder was imported, go to this folder
                Intent i = new Intent(SudokuImportActivity.this,
                        SudokuListActivity.class);
                i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, folderId);
                startActivity(i);
            }
        }
        // call finish, so this activity won't be part of history
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.import_sudoku);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.mipmap.ic_launcher);

        ProgressBar progressBar = findViewById(R.id.progress);

        AbstractImportTask importTask;
        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        if (dataUri != null) {
            Log.v(TAG, dataUri.toString());
            InputStreamReader streamReader = null;
            if (dataUri.getScheme().equals("content")) {
                try {
                    streamReader = new InputStreamReader(getContentResolver().openInputStream(dataUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                java.net.URI juri = null;
                try {
                    juri = new java.net.URI(dataUri.getScheme(), dataUri
                            .getSchemeSpecificPart(), dataUri.getFragment());
                    streamReader = new InputStreamReader(juri.toURL().openStream());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (streamReader == null) {
                return;
            }

            char[] cbuf = new char[512];

            try {
                // read first 512 bytes to check the type of file
                streamReader.read(cbuf, 0, 512);
                streamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String cbuf_str = new String(cbuf);

            if (cbuf_str.contains("<opensudoku")) {
                // Seems to be an OpenSudoku file
                importTask = new OpenSudokuImportTask(dataUri);
            } else if (cbuf_str.matches("[.0-9\\n\\r]{512}")) {
                // Seems to be a Sudoku SDM file
                importTask = new SdmImportTask(dataUri);
            } else {
                Log.e(
                        TAG,
                        String.format(
                                "Unknown type of data provided (mime-type=%s; uri=%s), exiting.",
                                intent.getType(), dataUri));
                finish();
                return;

            }
        } else if (intent.getStringExtra(EXTRA_FOLDER_NAME) != null) {

            String folderName = intent.getStringExtra(EXTRA_FOLDER_NAME);
            String games = intent.getStringExtra(EXTRA_GAMES);
            boolean appendToFolder = intent.getBooleanExtra(
                    EXTRA_APPEND_TO_FOLDER, false);
            importTask = new ExtrasImportTask(folderName, games, appendToFolder);

        } else {
            Log.e(TAG, "No data provided, exiting.");
            finish();
            return;
        }

        importTask.initialize(this, progressBar);
        importTask.setOnImportFinishedListener(mOnImportFinishedListener);

        importTask.execute();
    }

}
