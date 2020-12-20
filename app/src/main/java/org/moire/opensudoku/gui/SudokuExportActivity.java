package org.moire.opensudoku.gui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
    private static final int DIALOG_PROGRESS = 2;
    private static final String TAG = SudokuExportActivity.class.getSimpleName();
    private static final int CREATE_FILE = 1;
    /**
     * Id of sudoku to export.
     */
//	public static final String EXTRA_SUDOKU_ID = "SUDOKU_ID";

    private FileExportTask mFileExportTask;
    private FileExportTaskParams mExportParams;

    private EditText mFileNameEdit;
    private final OnClickListener mOnSaveClickListener = v -> exportToFile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_export);

        mFileNameEdit = findViewById(R.id.filename);
        EditText mDirectoryEdit = findViewById(R.id.directory);
        Button mSaveButton = findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(mOnSaveClickListener);

        mFileExportTask = new FileExportTask(this);
        mExportParams = new FileExportTaskParams();

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOLDER_ID)) {
            mExportParams.folderID = intent.getLongExtra(EXTRA_FOLDER_ID, ALL_FOLDERS);
//		} else if (intent.hasExtra(EXTRA_SUDOKU_ID)) {
//			mExportParams.sudokuID = intent.getLongExtra(EXTRA_SUDOKU_ID, 0);
        } else {
            Log.d(TAG, "No 'FOLDER_ID' extra provided, exiting.");
            finish();
            return;
        }

        String fileName;
        String timestamp = DateFormat.format("yyyy-MM-dd", new Date()).toString();
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
        mFileNameEdit.setText(fileName);


        //showDialog(DIALOG_SELECT_EXPORT_METHOD);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle(R.string.app_name);
                progressDialog.setMessage(getString(R.string.exporting));
                return progressDialog;
        }

        return null;
    }

    private void exportToFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-opensudoku");
        intent.putExtra(Intent.EXTRA_TITLE, mFileNameEdit.getText().toString() + ".opensudoku");
        startActivityForResult(intent, CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                startExportToFileTask(uri);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startExportToFileTask(Uri uri) {
        mFileExportTask.setOnExportFinishedListener(result -> {
            dismissDialog(DIALOG_PROGRESS);

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

        showDialog(DIALOG_PROGRESS);
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
