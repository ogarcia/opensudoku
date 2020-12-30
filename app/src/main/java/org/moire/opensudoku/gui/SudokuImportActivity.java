package org.moire.opensudoku.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.moire.opensudoku.R;
import org.moire.opensudoku.gui.importing.AbstractImportTask;
import org.moire.opensudoku.gui.importing.AbstractImportTask.OnImportFinishedListener;
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
    private static final String TAG = "ImportSudokuActivity";

    private final OnImportFinishedListener mOnImportFinishedListener = (importSuccessful, folderId) -> {
        if (importSuccessful) {
            Intent i;
            if (folderId == -1) {
                // multiple folders were imported, go to folder list
                i = new Intent(SudokuImportActivity.this,
                        FolderListActivity.class);
            } else {
                // one folder was imported, go to this folder
                i = new Intent(SudokuImportActivity.this,
                        SudokuListActivity.class);
                i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, folderId);
            }
            startActivity(i);
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
        String action = intent.getAction();
        Uri dataUri;
        if (action == null) {
            dataUri = intent.getData();
        }
        else if (action.equalsIgnoreCase("android.intent.action.SEND")) {
            dataUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
        }
        else if (action.equalsIgnoreCase("android.intent.action.VIEW")) {
            dataUri = intent.getData();
        }
        else {
            finish();
            return;
        }
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
                java.net.URI juri;
                Log.v(TAG, dataUri.toString());
                try {
                    juri = new java.net.URI(dataUri.getScheme(), dataUri
                            .getSchemeSpecificPart(), dataUri.getFragment());
                    streamReader = new InputStreamReader(juri.toURL().openStream());
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            }

            if (streamReader == null) {
                return;
            }

            char[] cbuf = new char[512];
            int read;
            try {
                // read first 512 bytes to check the type of file
                read = streamReader.read(cbuf, 0, 512);
                streamReader.close();
            } catch (IOException e) {
                return;
            }
            if (read < 81) {
                // At least one full 9x9 game needed in case of SDM
                return;
            }

            String cbuf_str = new String(cbuf);

            if (cbuf_str.contains("<opensudoku")) {
                // Seems to be an OpenSudoku file
                importTask = new OpenSudokuImportTask(dataUri);
            } else if (cbuf_str.matches("[.0-9\\n\\r]{" + read + "}")) {
                // Seems to be a Sudoku SDM file
                importTask = new SdmImportTask(dataUri);
            } else {
                Log.e(
                        TAG,
                        String.format(
                                "Unknown type of data provided (mime-type=%s; uri=%s), exiting.",
                                intent.getType(), dataUri));
                Toast.makeText(this, R.string.invalid_format, Toast.LENGTH_LONG).show();
                finish();
                return;

            }
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
