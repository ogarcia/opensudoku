package org.moire.opensudoku.gui.importing;

import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.SudokuGame;

/**
 * Handles import of puzzles via intent's extras.
 *
 * @author romario
 */
public class GenerateImportTask extends AbstractImportTask {

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

        for (int i=0; i<mNumGames; i++) {
            SudokuGame game = SudokuGame.generateNewGame(mNumEmptyCells);
            String data = game.toDataString();
            importGame(data);
        }

//        Generator g = new Generator();
//        for (int i=0; i<mNumGames; i++) {
//            String game = g.generateData(mNumEmptyCells);
//            importGame(game);
//        }
    }

}
