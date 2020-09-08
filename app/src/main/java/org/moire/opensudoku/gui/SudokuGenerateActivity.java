package org.moire.opensudoku.gui;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;

import org.moire.opensudoku.R;
import org.moire.opensudoku.gui.importing.GenerateImportTask;

import java.util.Date;

public class SudokuGenerateActivity extends ThemedActivity {

    private static final String TAG = SudokuGenerateActivity.class.getSimpleName();

    public static final String EXTRA_FOLDER_NAME = SudokuImportActivity.EXTRA_FOLDER_NAME;
    public static final String EXTRA_APPEND_TO_FOLDER = SudokuImportActivity.EXTRA_APPEND_TO_FOLDER;
    public static final String EXTRA_NUM_GAMES = SudokuImportActivity.EXTRA_NUM_GAMES;
    public static final String EXTRA_NUM_EMPTY_CELLS = SudokuImportActivity.EXTRA_NUM_EMPTY_CELLS;

    private EditText mFolderNameEdit;
    private NumberPicker mNumGamesPicker;
    private NumberPicker mNumEmptyCellsPicker;
    private CheckBox mAppendToFolderCb;

    private GenerateImportTask mGenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_gen);

        mFolderNameEdit = findViewById(R.id.gen_folder_name);
        mNumGamesPicker = findViewById(R.id.gen_games);
        mNumEmptyCellsPicker = findViewById(R.id.gen_empty_cells);
        mAppendToFolderCb = findViewById(R.id.gen_append);

        Intent intent = getIntent();
        String folderName = intent.getStringExtra(EXTRA_FOLDER_NAME);
        mFolderNameEdit.setText(folderName == null
                ? "Gen_"+DateFormat.format("yyyy-MM-dd-hh-mm-ss", new Date()).toString()+"_{games}x{empty}"
                : folderName);

        mNumEmptyCellsPicker.setMinValue(1);
        mNumEmptyCellsPicker.setMaxValue(81);
        mNumEmptyCellsPicker.setValue(intent.getIntExtra(EXTRA_NUM_EMPTY_CELLS, 55));

        mNumGamesPicker.setMinValue(1);
        mNumGamesPicker.setMaxValue(100);
        mNumGamesPicker.setValue(intent.getIntExtra(EXTRA_NUM_GAMES, 5));

        mAppendToFolderCb.setChecked(intent.getBooleanExtra(EXTRA_APPEND_TO_FOLDER, false));

        Button mGenButton = findViewById(R.id.gen_button);
        mGenButton.setOnClickListener(v -> {
            String folder = mFolderNameEdit.getText().toString()
                    .replace("{games}", Integer.toString(mNumGamesPicker.getValue()))
                    .replace("{empty}", Integer.toString(mNumEmptyCellsPicker.getValue()));

            Intent i = new Intent(this, SudokuImportActivity.class);
            i.putExtra(EXTRA_FOLDER_NAME, folder);
            i.putExtra(EXTRA_NUM_GAMES, mNumGamesPicker.getValue());
            i.putExtra(EXTRA_NUM_EMPTY_CELLS, mNumEmptyCellsPicker.getValue());
            i.putExtra(EXTRA_APPEND_TO_FOLDER, mAppendToFolderCb.isChecked());
            startActivity(i);
        });
    }
}
