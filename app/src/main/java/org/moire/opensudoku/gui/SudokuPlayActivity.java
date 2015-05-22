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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.game.SudokuGame.OnPuzzleSolvedListener;
import org.moire.opensudoku.gui.inputmethod.IMControlPanel;
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister;
import org.moire.opensudoku.gui.inputmethod.IMNumpad;
import org.moire.opensudoku.gui.inputmethod.IMPopup;
import org.moire.opensudoku.gui.inputmethod.IMSingleNumber;
import org.moire.opensudoku.utils.AndroidUtils;

/*
 */
public class SudokuPlayActivity extends Activity {

	public static final String EXTRA_SUDOKU_ID = "sudoku_id";

	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
	public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
	public static final int MENU_ITEM_UNDO = Menu.FIRST + 3;
	public static final int MENU_ITEM_HELP = Menu.FIRST + 4;
	public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 5;

	public static final int MENU_ITEM_SET_CHECKPOINT = Menu.FIRST + 6;
	public static final int MENU_ITEM_UNDO_TO_CHECKPOINT = Menu.FIRST + 7;

	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;
	private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;

	private static final int REQUEST_SETTINGS = 1;

	private long mSudokuGameID;
	private SudokuGame mSudokuGame;


	private SudokuDatabase mDatabase;

	private Handler mGuiHandler;

	private ViewGroup mRootLayout;
	private SudokuBoardView mSudokuBoard;
	private TextView mTimeLabel;

	private IMControlPanel mIMControlPanel;
	private IMControlPanelStatePersister mIMControlPanelStatePersister;
	private IMPopup mIMPopup;
	private IMSingleNumber mIMSingleNumber;
	private IMNumpad mIMNumpad;

	private boolean mShowTime = true;
	private GameTimer mGameTimer;
	private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
	private boolean mFullScreen;
	private boolean mFillInNotesEnabled = false;

	private HintsQueue mHintsQueue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
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

		setContentView(R.layout.sudoku_play);

		mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
		mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);
		mTimeLabel = (TextView) findViewById(R.id.time_label);

		mDatabase = new SudokuDatabase(getApplicationContext());
		mHintsQueue = new HintsQueue(this);
		mGameTimer = new GameTimer();

		mGuiHandler = new Handler();

		// create sudoku game instance
		if (savedInstanceState == null) {
			// activity runs for the first time, read game from database
			mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0);
			mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		} else {
			// activity has been running before, restore its state
			mSudokuGame = new SudokuGame();
			mSudokuGame.restoreState(savedInstanceState);
			mGameTimer.restoreState(savedInstanceState);
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			mSudokuGame.start();
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
			mSudokuBoard.setReadOnly(true);
		}

		mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

		mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);

		mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
		mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);

		mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

		mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
		mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
		mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// read game settings
		SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		int screenPadding = gameSettings.getInt("screen_border_size", 0);
		mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);

		mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", false);

		mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
		mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

		mShowTime = gameSettings.getBoolean("show_time", true);
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();

			if (mShowTime) {
				mGameTimer.start();
			}
		}
		mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE : View.GONE);

		mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
		mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
		mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
		mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
		mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
		mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
		mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
		mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
		mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
		mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

		mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
		mIMControlPanelStatePersister.restoreState(mIMControlPanel);

		updateTime();
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

		// we will save game to the database as we might not be able to get back
		mDatabase.updateSudoku(mSudokuGame);

		mGameTimer.stop();
		mIMControlPanel.pause();
		mIMControlPanelStatePersister.saveState(mIMControlPanel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mGameTimer.stop();

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.pause();
		}

		mSudokuGame.saveState(outState);
		mGameTimer.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo)
				.setShortcut('1', 'u')
				.setIcon(R.drawable.ic_undo);

		menu.add(0, MENU_ITEM_RESTART, 5, R.string.restart)
				.setShortcut('7', 'r')
				.setIcon(R.drawable.ic_restore);

		menu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 2, R.string.clear_all_notes)
				.setShortcut('3', 'a')
				.setIcon(R.drawable.ic_delete);

		if (mFillInNotesEnabled) {
			menu.add(0, MENU_ITEM_FILL_IN_NOTES, 1, R.string.fill_in_notes)
					.setIcon(R.drawable.ic_edit_grey);
		}

        menu.add(0, MENU_ITEM_SET_CHECKPOINT, 3, R.string.set_checkpoint);
        menu.add(0, MENU_ITEM_UNDO_TO_CHECKPOINT, 4, R.string.undo_to_checkpoint);

		menu.add(0, MENU_ITEM_HELP, 7, R.string.help)
				.setShortcut('0', 'h')
				.setIcon(R.drawable.ic_help);

		menu.add(0, MENU_ITEM_SETTINGS, 6, R.string.settings)
				.setShortcut('9', 's')
				.setIcon(R.drawable.ic_settings);

		// Generate any additional actions that can be performed on the
		// overall list.  In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, SudokuPlayActivity.class), null, intent, 0, null);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(true);
			if (mFillInNotesEnabled) {
				menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(true);
			}
			menu.findItem(MENU_ITEM_UNDO).setEnabled(mSudokuGame.hasSomethingToUndo());
			menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(mSudokuGame.hasUndoCheckpoint());
		} else {
			menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(false);
			if (mFillInNotesEnabled) {
				menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(false);
			}
			menu.findItem(MENU_ITEM_UNDO).setEnabled(false);
			menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_RESTART:
				showDialog(DIALOG_RESTART);
				return true;
			case MENU_ITEM_CLEAR_ALL_NOTES:
				showDialog(DIALOG_CLEAR_NOTES);
				return true;
			case MENU_ITEM_FILL_IN_NOTES:
				mSudokuGame.fillInNotes();
				return true;
			case MENU_ITEM_UNDO:
				mSudokuGame.undo();
				return true;
			case MENU_ITEM_SETTINGS:
				Intent i = new Intent();
				i.setClass(this, GameSettingsActivity.class);
				startActivityForResult(i, REQUEST_SETTINGS);
				return true;
			case MENU_ITEM_HELP:
				mHintsQueue.showHint(R.string.help, R.string.help_text);
				return true;
			case MENU_ITEM_SET_CHECKPOINT:
				mSudokuGame.setUndoCheckpoint();
				return true;
			case MENU_ITEM_UNDO_TO_CHECKPOINT:
				showDialog(DIALOG_UNDO_TO_CHECKPOINT);
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SETTINGS:
				restartActivity();
				break;
		}
	}

	/**
	 * Restarts whole activity.
	 */
	private void restartActivity() {
		startActivity(getIntent());
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_WELL_DONE:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_info)
						.setTitle(R.string.well_done)
						.setMessage(getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())))
						.setPositiveButton(android.R.string.ok, null)
						.create();
			case DIALOG_RESTART:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_restore)
						.setTitle(R.string.app_name)
						.setMessage(R.string.restart_confirm)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Restart game
								mSudokuGame.reset();
								mSudokuGame.start();
								mSudokuBoard.setReadOnly(false);
								if (mShowTime) {
									mGameTimer.start();
								}
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create();
			case DIALOG_CLEAR_NOTES:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_delete)
						.setTitle(R.string.app_name)
						.setMessage(R.string.clear_all_notes_confirm)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								mSudokuGame.clearAllNotes();
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create();
			case DIALOG_UNDO_TO_CHECKPOINT:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_undo)
						.setTitle(R.string.app_name)
						.setMessage(R.string.undo_to_checkpoint_confirm)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								mSudokuGame.undoToCheckpoint();
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.create();

		}
		return null;
	}

	/**
	 * Occurs when puzzle is solved.
	 */
	private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

		@Override
		public void onPuzzleSolved() {
			mSudokuBoard.setReadOnly(true);
			showDialog(DIALOG_WELL_DONE);
		}

	};

	/**
	 * Update the time of game-play.
	 */
	void updateTime() {
		if (mShowTime) {
			setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
			mTimeLabel.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
		} else {
			setTitle(R.string.app_name);
		}

	}

	// This class implements the game clock.  All it does is update the
	// status each tick.
	private final class GameTimer extends Timer {

		GameTimer() {
			super(1000);
		}

		@Override
		protected boolean step(int count, long time) {
			updateTime();

			// Run until explicitly stopped.
			return false;
		}

	}
}
