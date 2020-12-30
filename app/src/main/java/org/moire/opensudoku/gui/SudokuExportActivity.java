package org.moire.opensudoku.gui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.gui.exporting.FileExportTask;
import org.moire.opensudoku.gui.exporting.FileExportTaskParams;

import java.io.FileNotFoundException;
import java.util.Date;

public class SudokuExportActivity extends ThemedActivity {
    /**
     * Id of folder to export. If -1, all folders will be exported.
     */
    public static final String EXTRA_FOLDER_ID = "FOLDER_ID";
    public static final long ALL_FOLDERS = -1;
    private static final String TAG = SudokuExportActivity.class.getSimpleName();
    private static final int CREATE_FILE = 1;

    private FileExportTask mFileExportTask;
    private FileExportTaskParams mExportParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_export);

        mFileExportTask = new FileExportTask(this);
        mExportParams = new FileExportTaskParams();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOLDER_ID)) {
            mExportParams.folderID = intent.getLongExtra(EXTRA_FOLDER_ID, ALL_FOLDERS);
        } else {
            Log.d(TAG, "No 'FOLDER_ID' extra provided, exiting.");
            finish();
            return;
        }

        String fileName;
        String timestamp = DateFormat.format("yyyy-MM-dd-HH-mm-ss", new Date()).toString();
        if (mExportParams.folderID == -1) {
            fileName = "all-folders-" + timestamp;
        } else {
            SudokuDatabase database = new SudokuDatabase(getApplicationContext());
            FolderInfo folder = database.getFolderInfo(mExportParams.folderID);
            if (folder == null) {
                Log.d(TAG, String.format("Folder with id %s not found, exiting.", mExportParams.folderID));
                finish();
                return;
            }
            fileName = folder.name + "-" + timestamp;
            database.close();
        }

        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-opensudoku");
        intent.putExtra(Intent.EXTRA_TITLE, fileName + ".opensudoku");
        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                startExportToFileTask(uri);
            }
        } else if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startExportToFileTask(Uri uri) {
        mFileExportTask.setOnExportFinishedListener(result -> {
            if (result.successful) {
                Toast.makeText(SudokuExportActivity.this, getString(
                        R.string.puzzles_have_been_exported, result.filename), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SudokuExportActivity.this, getString(
                        R.string.unknown_export_error), Toast.LENGTH_LONG).show();
            }
            finish();
        });
        try {
            mExportParams.file = getContentResolver().openOutputStream(uri);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                mExportParams.filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
            assert cursor != null;
            cursor.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(SudokuExportActivity.this, getString(
                    R.string.unknown_export_error), Toast.LENGTH_LONG).show();
        }
        mFileExportTask.execute(mExportParams);
    }
/*
    private void exportToMail() {

        mFileExportTask.setOnExportFinishedListener(result -> {
            mProgressDialog.dismiss();

            if (result.successful) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Const.MIME_TYPE_OPENSUDOKU);
                intent.putExtra(Intent.EXTRA_TEXT, "Puzzles attached.");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(result.file));

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SudokuExportActivity.this, "no way to share folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SudokuExportActivity.this, "not successful.", Toast.LENGTH_LONG).show();
            }

            finish();
        });

        mProgressDialog.show();
        mFileExportTask.execute(mExportParams);
    }
*/

}
