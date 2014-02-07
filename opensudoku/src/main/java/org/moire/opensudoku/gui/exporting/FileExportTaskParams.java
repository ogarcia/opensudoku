package org.moire.opensudoku.gui.exporting;

import java.io.File;

public class FileExportTaskParams {

	/**
	 * Id of folder to export. Set to -1, if you want to export all folders.
	 */
	public Long folderID;
	/**
	 * Id of sudoku puzzle to export.
	 */
	public Long sudokuID;

	/**
	 * File where data should be saved.
	 */
	public File file;

}
