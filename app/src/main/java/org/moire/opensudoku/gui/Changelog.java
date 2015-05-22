package org.moire.opensudoku.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.webkit.WebView;
import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.AndroidUtils;

public class Changelog {

	private static final String TAG = "Changelog";

	private static final String PREF_FILE_CHANGELOG = "changelog";

	private Context mContext;
	private SharedPreferences mPrefs;

	public Changelog(Context context) {
		mContext = context;
		mPrefs = mContext.getSharedPreferences(PREF_FILE_CHANGELOG, Context.MODE_PRIVATE);
	}

	public void showOnFirstRun() {
		String versionKey = "changelog_" + AndroidUtils.getAppVersionCode(mContext);

		if (!mPrefs.getBoolean(versionKey, false)) {
			showChangelogDialog();

			Editor editor = mPrefs.edit();
			editor.putBoolean(versionKey, true);
			editor.commit();
		}
	}

	private void showChangelogDialog() {

		String changelog = getChangelogFromResources();

		WebView webView = new WebView(mContext);
		webView.loadData(changelog, "text/html", "utf-8");

		AlertDialog changelogDialog = new AlertDialog.Builder(mContext)
				.setIcon(R.drawable.ic_info)
				.setTitle(R.string.what_is_new)
				.setView(webView)
				.setPositiveButton(R.string.close, null).create();


		changelogDialog.show();
	}

	private String getChangelogFromResources() {
		InputStream is = null;
		try {
			is = mContext.getResources().openRawResource(R.raw.changelog);

			final char[] buffer = new char[0x10000];
			StringBuilder out = new StringBuilder();
			Reader in = new InputStreamReader(is, "UTF-8");
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if (read > 0) {
					out.append(buffer, 0, read);
				}
			} while (read >= 0);

			return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Error when reading changelog from raw resources.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Error when reading changelog from raw resources.", e);
				}
			}
		}

		return "";
	}


}
