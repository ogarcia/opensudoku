package org.moire.opensudoku.db;

import org.moire.opensudoku.game.SudokuGame;

public class SudokuImportParams {
	public long created;
	public long state;
	public long time;
	public long lastPlayed;
	public String data;
	public String note;

	public void clear() {
		created = 0;
		state = SudokuGame.GAME_STATE_NOT_STARTED;
		time = 0;
		lastPlayed = 0;
		data = null;
		note = null;
	}
}
