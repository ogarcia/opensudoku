/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.moire.opensudoku.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.inputmethod.IMControlPanel;
import org.moire.opensudoku.gui.inputmethod.InputMethod;
import org.moire.opensudoku.utils.AndroidUtils;

/**
 * Activity for editing content of puzzle.
 *
 * @author romario
 */
public class SudokuEditActivity extends Activity {

	/**
	 * When inserting new data, I need to know folder in which will new sudoku be stored.
	 */
	public static final String EXTRA_FOLDER_ID = "folder_id";
	public static final String EXTRA_SUDOKU_ID = "sudoku_id";

	public static final int MENU_ITEM_SAVE = Menu.FIRST;
	public static final int MENU_ITEM_CANCEL = Menu.FIRST + 1;

	// The different distinct states the activity can be run in.
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;
	private static final int STATE_CANCEL = 2;

	private static final String TAG = "SudokuEditActivity";

	private int mState;
	private long mFolderID;
	private long mSudokuID;

	private SudokuDatabase mDatabase;
	private SudokuGame mGame;
	private ViewGroup mRootLayout;
	private SudokuBoardView mBoard;
	private IMControlPanel mInputMethods;
	private Handler mGuiHandler;

	private boolean mFullScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// go fullscreen for devices with QVGA screen (only way I found
		// how to fit UI on the screen)
		Display display = getWindowManager().getDefaultDisplay();
		if ((display.getWidth() == 240 || display.getWidth() == 320)
				&& (display.getHeight() == 240 || display.getHeight() == 320)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mFullScreen = true;
		}

		// theme must be set before setContentView
		AndroidUtils.setThemeFromPreferences(this);

		setContentView(R.layout.sudoku_edit);
		mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
		mBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);

		mDatabase = new SudokuDatabase(getApplicationContext());

		mGuiHandler = new Handler();

		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			// Requested to edit: set that state, and the data being edited.
			mState = STATE_EDIT;
			if (intent.hasExtra(EXTRA_SUDOKU_ID)) {
				mSudokuID = intent.getLongExtra(EXTRA_SUDOKU_ID, 0);
			} else {
				throw new IllegalArgumentException(String.format("Extra with key '%s' is required.", EXTRA_SUDOKU_ID));
			}
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mSudokuID = 0;

			if (intent.hasExtra(EXTRA_FOLDER_ID)) {
				mFolderID = intent.getLongExtra(EXTRA_FOLDER_ID, 0);
			} else {
				throw new IllegalArgumentException(String.format("Extra with key '%s' is required.", EXTRA_FOLDER_ID));
			}

		} else {
			// Whoops, unknown action!  Bail.
			Log.e(TAG, "Unknown action, exiting.");
			finish();
			return;
		}

		if (savedInstanceState != null) {
			mGame = new SudokuGame();
			mGame.restoreState(savedInstanceState);
		} else {
			if (mSudokuID != 0) {
				// existing sudoku, read it from database
				mGame = mDatabase.getSudoku(mSudokuID);
				mGame.getCells().markAllCellsAsEditable();
			} else {
				mGame = SudokuGame.createEmptyGame();
			}
		}
		mBoard.setGame(mGame);

		mInputMethods = (IMControlPanel) findViewById(R.id.input_methods);
		mInputMethods.initialize(mBoard, mGame, null);

		// only numpad input method will be enabled
		for (InputMethod im : mInputMethods.getInputMethods()) {
			im.setEnabled(false);
		}
		mInputMethods.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD).setEnabled(true);
		mInputMethods.activateInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// FIXME: When activity is resumed, title isn't sometimes hidden properly (there is black 
			// empty space at the top of the screen). This is desperate workaround.
			if (mFullScreen) {
				mGuiHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						mRootLayout.requestLayout();
					}
				}, 1000);
			}

		}
	}


	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing() && mState != STATE_CANCEL && !mGame.getCells().isEmpty()) {
			savePuzzle();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mGame.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_SAVE, 0, R.string.save)
				.setShortcut('1', 's')
				.setIcon(R.drawable.ic_save);
		menu.add(0, MENU_ITEM_CANCEL, 1, android.R.string.cancel)
				.setShortcut('3', 'c')
				.setIcon(R.drawable.ic_close);

		// Generate any additional actions that can be performed on the
		// overall list.  In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, SudokuEditActivity.class), null, intent, 0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_SAVE:
				// do nothing, puzzle will be saved automatically in onPause
				finish();
				return true;
			case MENU_ITEM_CANCEL:
				mState = STATE_CANCEL;
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void savePuzzle() {
		mGame.getCells().markFilledCellsAsNotEditable();

		switch (mState) {
			case STATE_EDIT:
				mDatabase.updateSudoku(mGame);
				Toast.makeText(getApplicationContext(), R.string.puzzle_updated, Toast.LENGTH_SHORT).show();
				break;
			case STATE_INSERT:
				mGame.setCreated(System.currentTimeMillis());
				mDatabase.insertSudoku(mFolderID, mGame);
				Toast.makeText(getApplicationContext(), R.string.puzzle_inserted, Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
