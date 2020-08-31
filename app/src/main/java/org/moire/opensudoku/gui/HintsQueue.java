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

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.moire.opensudoku.R;

import java.util.LinkedList;
import java.util.Queue;

public class HintsQueue {
    private static final String PREF_FILE_NAME = "hints";
    // TODO: should be persisted in activity's state
    private final Queue<Message> mMessages;
    private final AlertDialog mHintDialog;
    private Context mContext;
    private SharedPreferences mPrefs;
    private boolean mOneTimeHintsEnabled;

    public HintsQueue(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        gameSettings.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals("show_hints")) {
                mOneTimeHintsEnabled = sharedPreferences.getBoolean("show_hints", true);
            }
        });
        mOneTimeHintsEnabled = gameSettings.getBoolean("show_hints", true);

        //processQueue();
        OnClickListener mHintClosed = (dialog, which) -> {
            //processQueue();
        };
        mHintDialog = new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_info)
                .setTitle(R.string.hint)
                .setMessage("")
                .setPositiveButton(R.string.close, mHintClosed).create();

        mHintDialog.setOnDismissListener(dialog -> processQueue());

        mMessages = new LinkedList<>();
    }

    private void addHint(Message hint) {
        synchronized (mMessages) {
            mMessages.add(hint);
        }

        synchronized (mHintDialog) {
            if (!mHintDialog.isShowing()) {
                processQueue();
            }
        }
    }

    private void processQueue() {
        Message hint;

        synchronized (mMessages) {
            hint = mMessages.poll();
        }

        if (hint != null) {
            showHintDialog(hint);
        }
    }

    private void showHintDialog(Message hint) {
        synchronized (mHintDialog) {
            mHintDialog.setTitle(mContext.getString(hint.titleResID));
            mHintDialog.setMessage(mContext.getText(hint.messageResID));
            mHintDialog.show();
        }
    }

    public void showHint(int titleResID, int messageResID, Object... args) {
        Message hint = new Message();
        hint.titleResID = titleResID;
        hint.messageResID = messageResID;
        //hint.args = args;
        addHint(hint);
    }

    public void showOneTimeHint(String key, int titleResID, int messageResID, Object... args) {
        if (mOneTimeHintsEnabled) {

            // FIXME: remove in future versions
            // Before 1.0.0, hintKey was created from messageResID. This ID has in 1.0.0 changed.
            // From 1.0.0, hintKey is based on key, to be backward compatible, check for old
            // hint keys.
            if (legacyHintsWereDisplayed()) {
                return;
            }

            String hintKey = "hint_" + key;
            if (!mPrefs.getBoolean(hintKey, false)) {
                showHint(titleResID, messageResID, args);
                Editor editor = mPrefs.edit();
                editor.putBoolean(hintKey, true);
                editor.apply();
            }
        }

    }

    public boolean legacyHintsWereDisplayed() {
        return mPrefs.getBoolean("hint_2131099727", false) &&
                mPrefs.getBoolean("hint_2131099730", false) &&
                mPrefs.getBoolean("hint_2131099726", false) &&
                mPrefs.getBoolean("hint_2131099729", false) &&
                mPrefs.getBoolean("hint_2131099728", false);
    }

    public void resetOneTimeHints() {
        Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * This should be called when activity is paused.
     */
    public void pause() {
        // get rid of WindowLeakedException in logcat
        if (mHintDialog != null) {
            mHintDialog.cancel();
        }
    }

    private static class Message {
        int titleResID;
        int messageResID;
        //Object[] args;
    }

}
