/* 
 * Copyright (C) 2010 Vit Hnilica
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

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import org.moire.opensudoku.R;

/**
 * List folders.
 *
 * @author dracula
 */
public class FileListActivity extends ListActivity {
	private static final int DIALOG_IMPORT_FILE = 0;

	public static final String EXTRA_FOLDER_NAME = "FOLDER_NAME";

	public static final String ITEM_KEY_FILE = "file";
	public static final String ITEM_KEY_NAME = "name";
	public static final String ITEM_KEY_DETAIL = "detail";

	// input parameters for dialogs
	private String mDirectory;
	private File mSelectedFile;
	private List<Map<String, Object>> mList;
	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.file_list);

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		Intent intent = getIntent();
		String mDirectory = intent.getStringExtra(EXTRA_FOLDER_NAME);
		File selected_dir = new File(mDirectory);

		File[] dirs = selected_dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.isHidden() && pathname.canRead();
			}
		});

		File[] files = selected_dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && !pathname.isHidden() && pathname.canRead() && (pathname.getName().endsWith(".opensudoku") || pathname.getName().endsWith(".sdm"));
			}
		});

		Arrays.sort(dirs);
		Arrays.sort(files);

		mList = new ArrayList<Map<String, Object>>();
		if (selected_dir.getParentFile() != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ITEM_KEY_FILE, selected_dir.getParentFile());
			map.put(ITEM_KEY_NAME, "build/source/rs");
			mList.add(map);
		}
		for (File f : dirs) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ITEM_KEY_FILE, f);
			map.put(ITEM_KEY_NAME, f.getName());
			mList.add(map);
		}
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
		for (File f : files) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ITEM_KEY_FILE, f);
			map.put(ITEM_KEY_NAME, f.getName());
			Date date = new Date(f.lastModified());
			map.put(ITEM_KEY_DETAIL, dateFormat.format(date) + " " + timeFormat.format(date));
			mList.add(map);
		}
		String[] from = {ITEM_KEY_NAME, ITEM_KEY_DETAIL};
		int[] to = {R.id.name, R.id.detail};

		SimpleAdapter adapter = new SimpleAdapter(this, (List<? extends Map<String, ?>>) mList, R.layout.folder_list_item, from, to);
		adapter.setViewBinder(new FileListViewBinder());
		setListAdapter(adapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mDirectory", mDirectory);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		mDirectory = state.getString("mDirectory");
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		LayoutInflater.from(this);
		switch (id) {
			case DIALOG_IMPORT_FILE:
				return new AlertDialog.Builder(this)
						.setIcon(R.drawable.ic_cloud_upload)
						.setTitle(R.string.import_file)
						.setPositiveButton(R.string.import_file, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								//importovani
								File f = mSelectedFile;
								Intent i = new Intent(mContext, ImportSudokuActivity.class);
								Uri u = Uri.fromFile(f);
								i.setData(u);
								startActivity(i);
								//finish();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.create();
		}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
			case DIALOG_IMPORT_FILE:
				dialog.setTitle(getString(R.string.import_file_title, mSelectedFile.getName()));
				break;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File f = (File) (mList.get((int) id).get(ITEM_KEY_FILE));
		if (f.isFile()) {
			mSelectedFile = f;
			showDialog(DIALOG_IMPORT_FILE);
		} else if (f.isDirectory()) {
			Intent intent = new Intent();
			intent.setClass(this, FileListActivity.class);
			intent.putExtra(FileListActivity.EXTRA_FOLDER_NAME, f.getAbsolutePath());
			startActivity(intent);
			//finish();
		}
	}

	private static class FileListViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(View view, Object data,
									String textRepresentation) {
			switch (view.getId()) {
				case R.id.detail:
					if (data == null) {
						final TextView detailView = (TextView) view;
						detailView.setVisibility(View.INVISIBLE);
						return true;
					}
			}
			return false;
		}

	}

}
