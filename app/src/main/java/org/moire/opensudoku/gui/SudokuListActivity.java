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

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuColumns;
import org.moire.opensudoku.db.SudokuDatabase;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.FolderDetailLoader.FolderDetailCallback;
import org.moire.opensudoku.utils.AndroidUtils;

/**
 * List of puzzles in folder.
 *
 * @author romario
 */
public class SudokuListActivity extends ListActivity {

	public static final String EXTRA_FOLDER_ID = "folder_id";

	public static final int MENU_ITEM_INSERT = Menu.FIRST;
	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
	public static final int MENU_ITEM_PLAY = Menu.FIRST + 3;
	public static final int MENU_ITEM_RESET = Menu.FIRST + 4;
	public static final int MENU_ITEM_EDIT_NOTE = Menu.FIRST + 5;
	public static final int MENU_ITEM_FILTER = Menu.FIRST + 6;
	public static final int MENU_ITEM_FOLDERS = Menu.FIRST + 7;

	private static final int DIALOG_DELETE_PUZZLE = 0;
	private static final int DIALOG_RESET_PUZZLE = 1;
	private static final int DIALOG_EDIT_NOTE = 2;
	private static final int DIALOG_FILTER = 3;

	private static final String FILTER_STATE_NOT_STARTED = "filter" + SudokuGame.GAME_STATE_NOT_STARTED;
	private static final String FILTER_STATE_PLAYING = "filter" + SudokuGame.GAME_STATE_PLAYING;
	private static final String FILTER_STATE_SOLVED = "filter" + SudokuGame.GAME_STATE_COMPLETED;

	private static final String TAG = "SudokuListActivity";

	private long mFolderID;

	// input parameters for dialogs
	private long mDeletePuzzleID;
	private long mResetPuzzleID;
	private long mEditNotePuzzleID;
	private TextView mEditNoteInput;
	private SudokuListFilter mListFilter;

	private TextView mFilterStatus;

	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	private SudokuDatabase mDatabase;
	private FolderDetailLoader mFolderDetailLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// theme must be set before setContentView
		AndroidUtils.setThemeFromPreferences(this);

		setContentView(R.layout.sudoku_list);
		mFilterStatus = (TextView) findViewById(R.id.filter_status);

		getListView().setOnCreateContextMenuListener(this);
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		mDatabase = new SudokuDatabase(getApplicationContext());
		mFolderDetailLoader = new FolderDetailLoader(getApplicationContext());

		Intent intent = getIntent();
		if (intent.hasExtra(EXTRA_FOLDER_ID)) {
			mFolderID = intent.getLongExtra(EXTRA_FOLDER_ID, 0);
		} else {
			Log.d(TAG, "No 'folder_id' extra provided, exiting.");
			finish();
			return;
		}

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mListFilter = new SudokuListFilter(getApplicationContext());
		mListFilter.showStateNotStarted = settings.getBoolean(FILTER_STATE_NOT_STARTED, true);
		mListFilter.showStatePlaying = settings.getBoolean(FILTER_STATE_PLAYING, true);
		mListFilter.showStateCompleted = settings.getBoolean(FILTER_STATE_SOLVED, true);

		mAdapter = new SimpleCursorAdapter(this, R.layout.sudoku_list_item,
				null, new String[]{SudokuColumns.DATA, SudokuColumns.STATE,
				SudokuColumns.TIME, SudokuColumns.LAST_PLAYED,
				SudokuColumns.CREATED, SudokuColumns.PUZZLE_NOTE},
				new int[]{R.id.sudoku_board, R.id.state, R.id.time,
						R.id.last_played, R.id.created, R.id.note});
		mAdapter.setViewBinder(new SudokuListViewBinder(this));
		updateList();
		setListAdapter(mAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mDatabase.close();
		mFolderDetailLoader.destroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong("mDeletePuzzleID", mDeletePuzzleID);
		outState.putLong("mResetPuzzleID", mResetPuzzleID);
		outState.putLong("mEditNotePuzzleID", mEditNotePuzzleID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		mDeletePuzzleID = state.getLong("mDeletePuzzleID");
		mResetPuzzleID = state.getLong("mResetPuzzleID");
		mEditNotePuzzleID = state.getLong("mEditNotePuzzleID");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// the puzzle list is naturally refreshed when the window
		// regains focus, so we only need to update the title
		updateTitle();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if there is no activity in history and back button was pressed, go
		// to FolderListActivity, which is the root activity.
		if (isTaskRoot() && keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.setClass(this, FolderListActivity.class);
			startActivity(i);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_FOLDERS, 0, R.string.folders).setShortcut('1', 'f')
				.setIcon(R.drawable.ic_sort);
		menu.add(0, MENU_ITEM_FILTER, 1, R.string.filter).setShortcut('1', 'f')
				.setIcon(R.drawable.ic_view);
		menu.add(0, MENU_ITEM_INSERT, 2, R.string.add_sudoku).setShortcut('3', 'a')
				.setIcon(R.drawable.ic_add);
		// I'm not sure this one is ready for release
//		menu.add(0, MENU_ITEM_GENERATE, 3, R.string.generate_sudoku).setShortcut('4', 'g')
//		.setIcon(R.drawable.ic_add);

		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, SudokuListActivity.class), null,
				intent, 0, null);

		return true;

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		switch (id) {
			case DIALOG_DELETE_PUZZLE:
				return new AlertDialog.Builder(this).setIcon(
						R.drawable.ic_delete).setTitle("Puzzle").setMessage(
						R.string.delete_puzzle_confirm)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										mDatabase.deleteSudoku(mDeletePuzzleID);
										updateList();
									}
								}).setNegativeButton(android.R.string.no, null).create();
			case DIALOG_EDIT_NOTE:

				LayoutInflater factory = LayoutInflater.from(this);
				final View noteView = factory.inflate(R.layout.sudoku_list_item_note,
						null);
				mEditNoteInput = (TextView) noteView.findViewById(R.id.note);
				return new AlertDialog.Builder(this).setIcon(
						R.drawable.ic_add).setTitle(R.string.edit_note)
						.setView(noteView).setPositiveButton(R.string.save,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										SudokuGame game = mDatabase.getSudoku(mEditNotePuzzleID);
										game.setNote(mEditNoteInput.getText()
												.toString());
										mDatabase.updateSudoku(game);
										updateList();
									}
								}).setNegativeButton(android.R.string.cancel, null).create();
			case DIALOG_RESET_PUZZLE:
				return new AlertDialog.Builder(this).setIcon(
						R.drawable.ic_restore).setTitle("Puzzle")
						.setMessage(R.string.reset_puzzle_confirm)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int whichButton) {
										SudokuGame game = mDatabase.getSudoku(mResetPuzzleID);
										if (game != null) {
											game.reset();
											mDatabase.updateSudoku(game);
										}
										updateList();
									}
								}).setNegativeButton(android.R.string.no, null).create();
			case DIALOG_FILTER:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_view)
						.setTitle(R.string.filter_by_gamestate)
						.setMultiChoiceItems(
								R.array.game_states,
								new boolean[]{
										mListFilter.showStateNotStarted,
										mListFilter.showStatePlaying,
										mListFilter.showStateCompleted,
								},
								new DialogInterface.OnMultiChoiceClickListener() {
									public void onClick(DialogInterface dialog, int whichButton,
														boolean isChecked) {
										switch (whichButton) {
											case 0:
												mListFilter.showStateNotStarted = isChecked;
												break;
											case 1:
												mListFilter.showStatePlaying = isChecked;
												break;
											case 2:
												mListFilter.showStateCompleted = isChecked;
												break;
										}
									}
								})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								settings.edit()
										.putBoolean(FILTER_STATE_NOT_STARTED, mListFilter.showStateNotStarted)
										.putBoolean(FILTER_STATE_PLAYING, mListFilter.showStatePlaying)
										.putBoolean(FILTER_STATE_SOLVED, mListFilter.showStateCompleted)
										.commit();
								updateList();
							}
						})
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

								/* User clicked No so do some stuff */
							}
						}).create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
			case DIALOG_EDIT_NOTE: {
				SudokuDatabase db = new SudokuDatabase(getApplicationContext());
				SudokuGame game = db.getSudoku(mEditNotePuzzleID);
				mEditNoteInput.setText(game.getNote());
				break;
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
									ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		menu.setHeaderTitle("Puzzle");

		// Add a menu item to delete the note
		menu.add(0, MENU_ITEM_PLAY, 0, R.string.play_puzzle);
		menu.add(0, MENU_ITEM_EDIT_NOTE, 1, R.string.edit_note);
		menu.add(0, MENU_ITEM_RESET, 2, R.string.reset_puzzle);
		menu.add(0, MENU_ITEM_EDIT, 3, R.string.edit_puzzle);
		menu.add(0, MENU_ITEM_DELETE, 4, R.string.delete_puzzle);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
			case MENU_ITEM_PLAY:
				playSudoku(info.id);
				return true;
			case MENU_ITEM_EDIT:
				Intent i = new Intent(this, SudokuEditActivity.class);
				i.setAction(Intent.ACTION_EDIT);
				i.putExtra(SudokuEditActivity.EXTRA_SUDOKU_ID, info.id);
				startActivity(i);
				return true;
			case MENU_ITEM_DELETE:
				mDeletePuzzleID = info.id;
				showDialog(DIALOG_DELETE_PUZZLE);
				return true;
			case MENU_ITEM_EDIT_NOTE:
				mEditNotePuzzleID = info.id;
				showDialog(DIALOG_EDIT_NOTE);
				return true;
			case MENU_ITEM_RESET:
				mResetPuzzleID = info.id;
				showDialog(DIALOG_RESET_PUZZLE);
				return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ITEM_INSERT: {
				// Launch activity to insert a new item
				Intent i = new Intent(this, SudokuEditActivity.class);
				i.setAction(Intent.ACTION_INSERT);
				i.putExtra(SudokuEditActivity.EXTRA_FOLDER_ID, mFolderID);
				startActivity(i);
				return true;
			}
			case MENU_ITEM_FILTER:
				showDialog(DIALOG_FILTER);
				return true;
			case MENU_ITEM_FOLDERS: {
				Intent i = new Intent(this, FolderListActivity.class);
				startActivity(i);
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playSudoku(id);
	}

	/**
	 * Updates whole list.
	 */
	private void updateList() {
		updateTitle();
		updateFilterStatus();

		if (mCursor != null) {
			stopManagingCursor(mCursor);
		}
		mCursor = mDatabase.getSudokuList(mFolderID, mListFilter);
		startManagingCursor(mCursor);
		mAdapter.changeCursor(mCursor);
	}

	private void updateFilterStatus() {

		if (mListFilter.showStateCompleted && mListFilter.showStateNotStarted && mListFilter.showStatePlaying) {
			mFilterStatus.setVisibility(View.GONE);
		} else {
			mFilterStatus.setText(getString(R.string.filter_active, mListFilter));
			mFilterStatus.setVisibility(View.VISIBLE);
		}
	}

	private void updateTitle() {
		FolderInfo folder = mDatabase.getFolderInfo(mFolderID);
		setTitle(folder.name);

		mFolderDetailLoader.loadDetailAsync(mFolderID, new FolderDetailCallback() {
			@Override
			public void onLoaded(FolderInfo folderInfo) {
				if (folderInfo != null)
					setTitle(folderInfo.name + " - " + folderInfo.getDetail(getApplicationContext()));
			}
		});
	}

	private void playSudoku(long sudokuID) {
		Intent i = new Intent(SudokuListActivity.this, SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, sudokuID);
		startActivity(i);
	}

	private static class SudokuListViewBinder implements ViewBinder {
		private Context mContext;
		private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
		private DateFormat mDateTimeFormatter = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT);
		private DateFormat mTimeFormatter = DateFormat
				.getTimeInstance(DateFormat.SHORT);

		public SudokuListViewBinder(Context context) {
			mContext = context;
		}

		@Override
		public boolean setViewValue(View view, Cursor c, int columnIndex) {

			int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));

			TextView label = null;

			switch (view.getId()) {
				case R.id.sudoku_board:
					String data = c.getString(columnIndex);
					// TODO: still can be faster, I don't have to call initCollection and read notes
					CellCollection cells = null;
					;
					try {
						cells = CellCollection.deserialize(data);
					} catch (Exception e) {
						long id = c.getLong(c.getColumnIndex(SudokuColumns._ID));
						Log.e(TAG, String.format("Exception occurred when deserializing puzzle with id %s.", id), e);
					}
					SudokuBoardView board = (SudokuBoardView) view;
					board.setReadOnly(true);
					board.setFocusable(false);
					((SudokuBoardView) view).setCells(cells);
					break;
				case R.id.state:
					label = ((TextView) view);
					String stateString = null;
					switch (state) {
						case SudokuGame.GAME_STATE_COMPLETED:
							stateString = mContext.getString(R.string.solved);
							break;
						case SudokuGame.GAME_STATE_PLAYING:
							stateString = mContext.getString(R.string.playing);
							break;
					}
					label.setVisibility(stateString == null ? View.GONE
							: View.VISIBLE);
					label.setText(stateString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						// TODO: read colors from android resources
						label.setTextColor(Color.rgb(150, 150, 150));
					} else {
						label.setTextColor(Color.rgb(255, 255, 255));
						//label.setTextColor(SudokuListActivity.this.getResources().getColor(R.));
					}
					break;
				case R.id.time:
					long time = c.getLong(columnIndex);
					label = ((TextView) view);
					String timeString = null;
					if (time != 0) {
						timeString = mGameTimeFormatter.format(time);
					}
					label.setVisibility(timeString == null ? View.GONE
							: View.VISIBLE);
					label.setText(timeString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						// TODO: read colors from android resources
						label.setTextColor(Color.rgb(150, 150, 150));
					} else {
						label.setTextColor(Color.rgb(255, 255, 255));
					}
					break;
				case R.id.last_played:
					long lastPlayed = c.getLong(columnIndex);
					label = ((TextView) view);
					String lastPlayedString = null;
					if (lastPlayed != 0) {
						lastPlayedString = mContext.getString(R.string.last_played_at,
								getDateAndTimeForHumans(lastPlayed));
					}
					label.setVisibility(lastPlayedString == null ? View.GONE
							: View.VISIBLE);
					label.setText(lastPlayedString);
					break;
				case R.id.created:
					long created = c.getLong(columnIndex);
					label = ((TextView) view);
					String createdString = null;
					if (created != 0) {
						createdString = mContext.getString(R.string.created_at,
								getDateAndTimeForHumans(created));
					}
					// TODO: when GONE, note is not correctly aligned below last_played
					label.setVisibility(createdString == null ? View.GONE
							: View.VISIBLE);
					label.setText(createdString);
					break;
				case R.id.note:
					String note = c.getString(columnIndex);
					label = ((TextView) view);
					if (note == null || note.trim() == "") {
						((TextView) view).setVisibility(View.GONE);
					} else {
						((TextView) view).setText(note);
					}
					label
							.setVisibility((note == null || note.trim().equals("")) ? View.GONE
									: View.VISIBLE);
					label.setText(note);
					break;
			}

			return true;
		}

		private String getDateAndTimeForHumans(long datetime) {
			Date date = new Date(datetime);

			Date now = new Date(System.currentTimeMillis());
			Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
			Date yesterday = new Date(System.currentTimeMillis()
					- (1000 * 60 * 60 * 24));

			if (date.after(today)) {
				return mContext.getString(R.string.at_time, mTimeFormatter.format(date));
			} else if (date.after(yesterday)) {
				return mContext.getString(R.string.yesterday_at_time, mTimeFormatter.format(date));
			} else {
				return mContext.getString(R.string.on_date, mDateTimeFormatter.format(date));
			}

		}
	}

}
