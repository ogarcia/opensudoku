package org.moire.opensudoku.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.moire.opensudoku.R;
import org.moire.opensudoku.gui.importing.GenerateImportTask;

import java.util.Date;

public class SudokuDownloadActivity extends ThemedActivity {

    private static final String TAG = SudokuDownloadActivity.class.getSimpleName();

    public static final String EXTRA_FOLDER_NAME = SudokuImportActivity.EXTRA_FOLDER_NAME;
    public static final String EXTRA_APPEND_TO_FOLDER = SudokuImportActivity.EXTRA_APPEND_TO_FOLDER;

    private EditText mFolderNameEdit;
    private EditText mUrlEdit;
    private CheckBox mAppendToFolderCb;

    private GenerateImportTask mGenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_dl);

        mFolderNameEdit = findViewById(R.id.dl_folder_name);
        mUrlEdit = findViewById(R.id.dl_url);
        mAppendToFolderCb = findViewById(R.id.dl_append);

        Intent intent = getIntent();
        String folderName = intent.getStringExtra(EXTRA_FOLDER_NAME);
        mFolderNameEdit.setText(folderName == null
                ? "Dl_"+DateFormat.format("yyyy-MM-dd-hh-mm-ss", new Date()).toString()
                : folderName);

        mAppendToFolderCb.setChecked(intent.getBooleanExtra(EXTRA_APPEND_TO_FOLDER, false));

        Button mDlButton = findViewById(R.id.dl_button);
        mDlButton.setOnClickListener(v -> {
            Intent i = new Intent(this, SudokuImportActivity.class);
            i.setData(Uri.parse(mUrlEdit.getText().toString()));
            i.putExtra(EXTRA_FOLDER_NAME, mFolderNameEdit.getText().toString());
            i.putExtra(EXTRA_APPEND_TO_FOLDER, mAppendToFolderCb.isChecked());
            startActivity(i);
        });
    }
}
