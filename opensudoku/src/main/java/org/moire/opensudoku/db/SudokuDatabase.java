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

package org.moire.opensudoku.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.FolderInfo;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.SudokuListFilter;

/**
 * Wrapper around opensudoku's database.
 * <p/>
 * You have to pass application context when creating instance:
 * <code>SudokuDatabase db = new SudokuDatabase(getApplicationContext());</code>
 * <p/>
 * You have to explicitly close connection when you're done with database (see {@link #close()}).
 * <p/>
 * This class supports database transactions using {@link #beginTransaction()}, \
 * {@link #setTransactionSuccessful()} and {@link #endTransaction()}.
 * See {@link SQLiteDatabase} for details on how to use them.
 *
 * @author romario
 */
public class SudokuDatabase {
	public static final String DATABASE_NAME = "opensudoku";


	public static final String SUDOKU_TABLE_NAME = "sudoku";
	public static final String FOLDER_TABLE_NAME = "folder";

	//private static final String TAG = "SudokuDatabase";

	private DatabaseHelper mOpenHelper;

	public SudokuDatabase(Context context) {
		mOpenHelper = new DatabaseHelper(context);
	}

	/**
	 * Returns list of puzzle folders.
	 *
	 * @return
	 */
	public Cursor getFolderList() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(FOLDER_TABLE_NAME);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, null, null, null, null, null, "created ASC");
	}

	/**
	 * Returns the folder info.
	 *
	 * @param folderID Primary key of folder.
	 * @return
	 */
	public FolderInfo getFolderInfo(long folderID) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(FOLDER_TABLE_NAME);
		qb.appendWhere(FolderColumns._ID + "=" + folderID);

		Cursor c = null;

		try {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			c = qb.query(db, null, null, null, null, null, null);

			if (c.moveToFirst()) {
				long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
				String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

				FolderInfo folderInfo = new FolderInfo();
				folderInfo.id = id;
				folderInfo.name = name;

				return folderInfo;
			} else {
				return null;
			}
		} finally {
			if (c != null) c.close();
		}
	}

	/**
	 * Returns the full folder info - this includes count of games in particular states.
	 *
	 * @param folderID Primary key of folder.
	 * @return
	 */
	public FolderInfo getFolderInfoFull(long folderID) {
		FolderInfo folder = null;

		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = mOpenHelper.getReadableDatabase();

			// selectionArgs: You may include ?s in where clause in the query, which will be replaced by the values from selectionArgs. The values will be bound as Strings.
			String q = "select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id where folder._id = " + folderID + " group by sudoku.state";
			c = db.rawQuery(q, null);

			while (c.moveToNext()) {
				long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
				String name = c.getString(c.getColumnIndex(FolderColumns.NAME));
				int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
				int count = c.getInt(c.getColumnIndex("count"));

				if (folder == null) {
					folder = new FolderInfo(id, name);
				}

				folder.puzzleCount += count;
				if (state == SudokuGame.GAME_STATE_COMPLETED) {
					folder.solvedCount += count;
				}
				if (state == SudokuGame.GAME_STATE_PLAYING) {
					folder.playingCount += count;
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}

		return folder;
	}

	private static final String INBOX_FOLDER_NAME = "Inbox";

	/**
	 * Returns folder which acts as a holder for puzzles imported without folder.
	 * If this folder does not exists, it is created.
	 *
	 * @return
	 */
	public FolderInfo getInboxFolder() {
		FolderInfo inbox = findFolder(INBOX_FOLDER_NAME);
		if (inbox != null) {
			inbox = insertFolder(INBOX_FOLDER_NAME, System.currentTimeMillis());
		}
		return inbox;
	}

	/**
	 * Find folder by name. If no folder is found, null is returned.
	 *
	 * @param folderName
	 * @param db
	 * @return
	 */
	public FolderInfo findFolder(String folderName) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(FOLDER_TABLE_NAME);
		qb.appendWhere(FolderColumns.NAME + " = ?");

		Cursor c = null;

		try {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			c = qb.query(db, null, null, new String[]{folderName}, null, null, null);

			if (c.moveToFirst()) {
				long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
				String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

				FolderInfo folderInfo = new FolderInfo();
				folderInfo.id = id;
				folderInfo.name = name;

				return folderInfo;
			} else {
				return null;
			}
		} finally {
			if (c != null) c.close();
		}
	}

	/**
	 * Inserts new puzzle folder into the database.
	 *
	 * @param name    Name of the folder.
	 * @param created Time of folder creation.
	 * @return
	 */
	public FolderInfo insertFolder(String name, Long created) {
		ContentValues values = new ContentValues();
		values.put(FolderColumns.CREATED, created);
		values.put(FolderColumns.NAME, name);

		long rowId;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		rowId = db.insert(FOLDER_TABLE_NAME, FolderColumns._ID, values);

		if (rowId > 0) {
			FolderInfo fi = new FolderInfo();
			fi.id = rowId;
			fi.name = name;
			return fi;
		}

		throw new SQLException(String.format("Failed to insert folder '%s'.", name));
	}

	/**
	 * Updates folder's information.
	 *
	 * @param folderID Primary key of folder.
	 * @param name     New name for the folder.
	 */
	public void updateFolder(long folderID, String name) {
		ContentValues values = new ContentValues();
		values.put(FolderColumns.NAME, name);

		SQLiteDatabase db = null;
		db = mOpenHelper.getWritableDatabase();
		db.update(FOLDER_TABLE_NAME, values, FolderColumns._ID + "=" + folderID, null);
	}

	/**
	 * Deletes given folder.
	 *
	 * @param folderID Primary key of folder.
	 */
	public void deleteFolder(long folderID) {

		// TODO: should run in transaction
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		// delete all puzzles in folder we are going to delete
		db.delete(SUDOKU_TABLE_NAME, SudokuColumns.FOLDER_ID + "=" + folderID, null);
		// delete the folder
		db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
	}

	/**
	 * Returns list of puzzles in the given folder.
	 *
	 * @param folderID Primary key of folder.
	 * @return
	 */
	public Cursor getSudokuList(long folderID, SudokuListFilter filter) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(SUDOKU_TABLE_NAME);
		//qb.setProjectionMap(sPlacesProjectionMap);
		qb.appendWhere(SudokuColumns.FOLDER_ID + "=" + folderID);

		if (filter != null) {
			if (!filter.showStateCompleted) {
				qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_COMPLETED);
			}
			if (!filter.showStateNotStarted) {
				qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_NOT_STARTED);
			}
			if (!filter.showStatePlaying) {
				qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_PLAYING);
			}
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return qb.query(db, null, null, null, null, null, "created DESC");
	}

	/**
	 * Returns sudoku game object.
	 *
	 * @param sudokuID Primary key of folder.
	 * @return
	 */
	public SudokuGame getSudoku(long sudokuID) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(SUDOKU_TABLE_NAME);
		qb.appendWhere(SudokuColumns._ID + "=" + sudokuID);

		// Get the database and run the query

		SQLiteDatabase db = null;
		Cursor c = null;
		SudokuGame s = null;
		try {
			db = mOpenHelper.getReadableDatabase();
			c = qb.query(db, null, null, null, null, null, null);

			if (c.moveToFirst()) {
				long id = c.getLong(c.getColumnIndex(SudokuColumns._ID));
				long created = c.getLong(c.getColumnIndex(SudokuColumns.CREATED));
				String data = c.getString(c.getColumnIndex(SudokuColumns.DATA));
				long lastPlayed = c.getLong(c.getColumnIndex(SudokuColumns.LAST_PLAYED));
				int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
				long time = c.getLong(c.getColumnIndex(SudokuColumns.TIME));
				String note = c.getString(c.getColumnIndex(SudokuColumns.PUZZLE_NOTE));

				s = new SudokuGame();
				s.setId(id);
				s.setCreated(created);
				s.setCells(CellCollection.deserialize(data));
				s.setLastPlayed(lastPlayed);
				s.setState(state);
				s.setTime(time);
				s.setNote(note);
			}
		} finally {
			if (c != null) c.close();
		}

		return s;

	}


	/**
	 * Inserts new puzzle into the database.
	 *
	 * @param folderID Primary key of the folder in which puzzle should be saved.
	 * @param sudoku
	 * @return
	 */
	public long insertSudoku(long folderID, SudokuGame sudoku) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
		values.put(SudokuColumns.CREATED, sudoku.getCreated());
		values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
		values.put(SudokuColumns.STATE, sudoku.getState());
		values.put(SudokuColumns.TIME, sudoku.getTime());
		values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
		values.put(SudokuColumns.FOLDER_ID, folderID);

		long rowId = db.insert(SUDOKU_TABLE_NAME, FolderColumns.NAME, values);
		if (rowId > 0) {
			return rowId;
		}

		throw new SQLException("Failed to insert sudoku.");
	}

	private SQLiteStatement mInsertSudokuStatement;

	public long importSudoku(long folderID, SudokuImportParams pars) throws SudokuInvalidFormatException {
		if (pars.data == null) {
			throw new SudokuInvalidFormatException(pars.data);
		}

		if (!CellCollection.isValid(pars.data, CellCollection.DATA_VERSION_PLAIN)) {
			if (!CellCollection.isValid(pars.data, CellCollection.DATA_VERSION_1)) {
				throw new SudokuInvalidFormatException(pars.data);
			}
		}

		if (mInsertSudokuStatement == null) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			mInsertSudokuStatement = db.compileStatement(
					"insert into sudoku (folder_id, created, state, time, last_played, data, puzzle_note) values (?, ?, ?, ?, ?, ?, ?)"
			);
		}

		mInsertSudokuStatement.bindLong(1, folderID);
		mInsertSudokuStatement.bindLong(2, pars.created);
		mInsertSudokuStatement.bindLong(3, pars.state);
		mInsertSudokuStatement.bindLong(4, pars.time);
		mInsertSudokuStatement.bindLong(5, pars.lastPlayed);
		mInsertSudokuStatement.bindString(6, pars.data);
		if (pars.note == null) {
			mInsertSudokuStatement.bindNull(7);
		} else {
			mInsertSudokuStatement.bindString(7, pars.note);
		}

		long rowId = mInsertSudokuStatement.executeInsert();
		if (rowId > 0) {
			return rowId;
		}

		throw new SQLException("Failed to insert sudoku.");
	}

	/**
	 * Returns List of sudokus to export.
	 *
	 * @param folderID Id of folder to export, -1 if all folders will be exported.
	 * @return
	 */
	public Cursor exportFolder(long folderID) {
		String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note from folder f left outer join sudoku s on f._id = s.folder_id";
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		if (folderID != -1) {
			query += " where f._id = ?";
		}
		return db.rawQuery(query, folderID != -1 ? new String[]{String.valueOf(folderID)} : null);
	}

	/**
	 * Returns one concrete sudoku to export. Folder context is not exported in this case.
	 *
	 * @param sudokuID
	 * @return
	 */
	public Cursor exportSudoku(long sudokuID) {
		String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note from sudoku s inner join folder f on s.folder_id = f._id where s._id = ?";
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return db.rawQuery(query, new String[]{String.valueOf(sudokuID)});
	}

	/**
	 * Updates sudoku game in the database.
	 *
	 * @param sudoku
	 */
	public void updateSudoku(SudokuGame sudoku) {
		ContentValues values = new ContentValues();
		values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
		values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
		values.put(SudokuColumns.STATE, sudoku.getState());
		values.put(SudokuColumns.TIME, sudoku.getTime());
		values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.update(SUDOKU_TABLE_NAME, values, SudokuColumns._ID + "=" + sudoku.getId(), null);
	}


	/**
	 * Deletes given sudoku from the database.
	 *
	 * @param sudokuID
	 */
	public void deleteSudoku(long sudokuID) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.delete(SUDOKU_TABLE_NAME, SudokuColumns._ID + "=" + sudokuID, null);
	}

	public void close() {
		if (mInsertSudokuStatement != null) {
			mInsertSudokuStatement.close();
		}

		mOpenHelper.close();
	}

	public void beginTransaction() {
		mOpenHelper.getWritableDatabase().beginTransaction();
	}

	public void setTransactionSuccessful() {
		mOpenHelper.getWritableDatabase().setTransactionSuccessful();
	}

	public void endTransaction() {
		mOpenHelper.getWritableDatabase().endTransaction();
	}
}
