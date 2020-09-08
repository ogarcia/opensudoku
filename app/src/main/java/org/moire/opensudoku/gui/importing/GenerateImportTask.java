package org.moire.opensudoku.gui.importing;

import android.util.Log;

import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.game.SudokuGenerator;

/**
 * Handles import of puzzles via intent's extras.
 *
 * @author romario
 */
public class GenerateImportTask extends AbstractImportTask {

    private static String TAG = GenerateImportTask.class.getName();

    private String mFolderName;
    private int mNumEmptyCells;
    private int mNumGames;
    private boolean mAppendToFolder;

    public GenerateImportTask(String folderName, int numGames, int numEmptyCells, boolean appendToFolder) {
        mFolderName = folderName;
        mNumEmptyCells = numEmptyCells;
        mNumGames = numGames;
        mAppendToFolder = appendToFolder;
    }

    @Override
    protected void processImport() throws SudokuInvalidFormatException {
        if (mAppendToFolder) {
            appendToFolder(mFolderName);
        } else {
            importFolder(mFolderName);
        }

        SudokuGenerator generator = new SudokuGenerator();
        for (int i=0; i<mNumGames; i++) {
            SudokuGame game = generator.generate(mNumEmptyCells);
            String data = game.toDataString();
            importGame(data);
        }

        Log.d(TAG, "processImport: Generating "+mNumGames+" games took "+generator.getTotalAttempts()+" attempts");
    }

}
