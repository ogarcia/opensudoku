package org.moire.opensudoku.gui.importing;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.db.SudokuImportParams;
import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.gui.ImportSudokuActivity;
import org.moire.opensudoku.utils.Const;

/**
 * To add support for new import source, do following:
 * <p/>
 * 1) Subclass this class. Any input parameters specific for your import should be put
 * in constructor of your class.
 * 2) In {@link #processImport()} method process your data source (parse file or maybe download
 * data from some other source) and save puzzles by calling
 * {@link #importFolder(String, boolean)} and {@link #importGame(String)} methods. Note
 * that <code>importFolder</code> must be called first, otherwise <code>importGame</code>
 * doesn't know where to put puzzles.
 * 3) Add code to {@link ImportSudokuActivity} which creates instance of your new class and
 * passes it input parameters.
 * <p/>
 * TODO: add cancel support
 *
 * @author romario
 */
public abstract class AbstractImportTask extends
		AsyncTask<Void, Integer, Boolean> {
	static final int NUM_OF_PROGRESS_UPDATES = 20;

	protected Context mContext;
	private ProgressBar mProgressBar;

	private OnImportFinishedListener mOnImportFinishedListener;

	private SudokuDatabase mDatabase;
	private FolderInfo mFolder; // currently processed folder
	private int mFolderCount; // count of processed folders
	private int mGameCount; //count of processed puzzles
	private String mImportError;
	private boolean mImportSuccessful;

	public void initialize(Context context, ProgressBar progressBar) {
		mContext = context;
		mProgressBar = progressBar;
	}

	public void setOnImportFinishedListener(OnImportFinishedListener listener) {
		mOnImportFinishedListener = listener;
	}

	@Override
	protected Boolean doInBackground(Void... params) {

		try {
			return processImportInternal();
		} catch (Exception e) {
			Log.e(Const.TAG, "Exception occurred during import.", e);
			setError(mContext.getString(R.string.unknown_import_error));
		}

		return false;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values.length == 2) {
			mProgressBar.setMax(values[1]);
		}
		mProgressBar.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {

			if (mFolderCount == 1) {
				Toast.makeText(mContext, mContext.getString(R.string.puzzles_saved, mFolder.name),
						Toast.LENGTH_LONG).show();
			} else if (mFolderCount > 1) {
				Toast.makeText(mContext, mContext.getString(R.string.folders_created, mFolderCount),
						Toast.LENGTH_LONG).show();
			}

		} else {
			Toast.makeText(mContext, mImportError, Toast.LENGTH_LONG).show();
		}

		if (mOnImportFinishedListener != null) {
			long folderId = -1;
			if (mFolderCount == 1) {
				folderId = mFolder.id;
			}
			mOnImportFinishedListener.onImportFinished(result, folderId);
		}
	}

	private Boolean processImportInternal() {
		mImportSuccessful = true;

		long start = System.currentTimeMillis();

		mDatabase = new SudokuDatabase(mContext);
		try {
			mDatabase.beginTransaction();

			// let subclass handle the import
			processImport();

			mDatabase.setTransactionSuccessful();
		} catch (SudokuInvalidFormatException e) {
			setError(mContext.getString(R.string.invalid_format));
		} finally {
			mDatabase.endTransaction();
			mDatabase.close();
			mDatabase = null;
		}


		if (mFolderCount == 0 && mGameCount == 0) {
			setError(mContext.getString(R.string.no_puzzles_found));
			return false;
		}

		long end = System.currentTimeMillis();

		Log.i(Const.TAG, String.format("Imported in %f seconds.",
				(end - start) / 1000f));

		return mImportSuccessful;
	}

	/**
	 * Subclasses should do all import work in this method.
	 *
	 * @return
	 */
	protected abstract void processImport() throws SudokuInvalidFormatException;


	/**
	 * Creates new folder and starts appending puzzles to this folder.
	 *
	 * @param name
	 */
	protected void importFolder(String name) {
		importFolder(name, System.currentTimeMillis());
	}


	/**
	 * Creates new folder and starts appending puzzles to this folder.
	 *
	 * @param name
	 * @param created
	 */
	protected void importFolder(String name, long created) {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mFolderCount++;

		mFolder = mDatabase.insertFolder(name, created);
	}

	/**
	 * Starts appending puzzles to the folder with given <code>name</code>. If such folder does
	 * not exist, this method creates new one.
	 *
	 * @param name
	 */
	protected void appendToFolder(String name) {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mFolderCount++;

		mFolder = null;
		mFolder = mDatabase.findFolder(name);
		if (mFolder == null) {
			mFolder = mDatabase.insertFolder(name, System.currentTimeMillis());
		}
	}

	private SudokuImportParams mImportParams = new SudokuImportParams();

	/**
	 * Imports game. Game will be stored in folder, which was set by
	 * {@link #importFolder(String, boolean)} or {@link #appendToFolder(String)}.
	 *
	 * @param game
	 * @throws SudokuInvalidFormatException
	 */
	protected void importGame(String data) throws SudokuInvalidFormatException {
		mImportParams.clear();
		mImportParams.data = data;
		importGame(mImportParams);
	}

	/**
	 * Imports game with all its fields.
	 *
	 * @param game Fields to import (state of game, created, etc.)
	 * @param data Data to import.
	 */
	protected void importGame(SudokuImportParams pars) throws SudokuInvalidFormatException {
		if (mDatabase == null) {
			throw new IllegalStateException("Database is not opened.");
		}

		mDatabase.importSudoku(mFolder.id, pars);
	}

	protected void setError(String error) {
		mImportError = error;
		mImportSuccessful = false;
	}

	public interface OnImportFinishedListener {
		/**
		 * Occurs when import is finished.
		 *
		 * @param importSuccessful Indicates whether import was successful.
		 * @param folderId         Contains id of imported folder, or -1 if multiple folders were imported.
		 */
		void onImportFinished(boolean importSuccessful, long folderId);
	}

}
