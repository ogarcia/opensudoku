package org.moire.opensudoku.gui.importing;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.GeneratedSudokuGame;

import java.util.ArrayList;
import java.util.List;

public class GameGeneratorTask extends AbstractImportTask {
    private static final String TAG = GameGeneratorTask.class.getSimpleName();

    private final int mNumEmptyCells;
    private final int mNumGames;
    private final boolean mRoughGen;
    private final List<Long> mGameIds;

    private ProgressDialog mProgress;

    public GameGeneratorTask(int numGames, int numEmptyCells, boolean roughGen) {
        this.mNumEmptyCells = numEmptyCells;
        this.mNumGames = numGames;
        this.mRoughGen = roughGen;
        this.mGameIds = new ArrayList<>();
    }

    public void initialize(Context context) {
        mProgress = new ProgressDialog(context);
        mProgress.setMessage("Generating...");
        super.initialize(context, null);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgress.show();
    }

    @Override
    protected void processImport() throws SudokuInvalidFormatException {
        int attempts = 0;

        this.appendToFolder("Generated");

        for (int games = 0; games < mNumGames; games++) {
            GeneratedSudokuGame game;
            if (mRoughGen) {
                game = new GeneratedSudokuGame();
                game.generate(mNumEmptyCells);
                attempts++;
            } else {
                do {
                    game = new GeneratedSudokuGame();
                    attempts++;
                } while (game.generate(mNumEmptyCells) != mNumEmptyCells);
            }
            String data = game.getCells().serialize(CellCollection.DATA_VERSION_PLAIN);
            Log.d(TAG, "Importing game: " + data);
            long gameId = importGame(data);
            mGameIds.add(gameId);
            this.publishProgress(games, mNumGames);
        }

        Log.d(TAG, "Generated " + mNumGames + " games in " + attempts + " attempts.");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mProgress.dismiss();
    }

    public long getNumGames() {
        return mGameIds.size();
    }

    public long getGameId(int idx) {
        return mGameIds.get(idx);
    }
}
