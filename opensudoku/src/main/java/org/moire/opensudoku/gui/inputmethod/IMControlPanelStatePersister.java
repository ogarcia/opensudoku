package org.moire.opensudoku.gui.inputmethod;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * This class is responsible for persisting of control panel's state.
 *
 * @author romario
 */
public class IMControlPanelStatePersister {

	private static final String PREFIX = IMControlPanel.class.getName();

	private SharedPreferences mPreferences;

	public IMControlPanelStatePersister(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void saveState(IMControlPanel controlPanel) {
		// save state of control panel itself
		StateBundle cpState = new StateBundle(mPreferences, PREFIX + "", true);
		cpState.putInt("activeMethodIndex", controlPanel.getActiveMethodIndex());
		cpState.commit();

		// save state of all input methods
		for (InputMethod im : controlPanel.getInputMethods()) {
			StateBundle outState = new StateBundle(mPreferences, PREFIX + "" + im.getInputMethodName(), true);
			im.onSaveState(outState);
			outState.commit();
		}
	}

	public void restoreState(IMControlPanel controlPanel) {
		// restore state of control panel itself
		StateBundle cpState = new StateBundle(mPreferences, PREFIX + "", false);
		int methodId = cpState.getInt("activeMethodIndex", 0);
		if (methodId != -1) {
			controlPanel.activateInputMethod(methodId);
		}

		// restore state of all input methods
		for (InputMethod im : controlPanel.getInputMethods()) {
			StateBundle savedState = new StateBundle(mPreferences, PREFIX + "" + im.getInputMethodName(), false);
			im.onRestoreState(savedState);
		}
	}

	/**
	 * This is basically wrapper around anything which is capable of storing
	 * state. Instance of this object will be passed to concrete input method's
	 * to store and retreive their state.
	 *
	 * @author romario
	 */
	public static class StateBundle {

		private final SharedPreferences mPreferences;
		private final Editor mPrefEditor;
		private final String mPrefix;
		private final boolean mEditable;

		public StateBundle(SharedPreferences preferences, String prefix,
						   boolean editable) {
			mPreferences = preferences;
			mPrefix = prefix;
			mEditable = editable;

			if (mEditable) {
				mPrefEditor = preferences.edit();
			} else {
				mPrefEditor = null;
			}
		}

		public boolean getBoolean(String key, boolean defValue) {
			return mPreferences.getBoolean(mPrefix + key, defValue);
		}

		public float getFloat(String key, float defValue) {
			return mPreferences.getFloat(mPrefix + key, defValue);
		}

		public int getInt(String key, int defValue) {
			return mPreferences.getInt(mPrefix + key, defValue);
		}

		public String getString(String key, String defValue) {
			return mPreferences.getString(mPrefix + key, defValue);
		}

		public void putBoolean(String key, boolean value) {
			if (!mEditable) {
				throw new IllegalStateException("StateBundle is not editable");
			}
			mPrefEditor.putBoolean(mPrefix + key, value);
		}

		public void putFloat(String key, float value) {
			if (!mEditable) {
				throw new IllegalStateException("StateBundle is not editable");
			}
			mPrefEditor.putFloat(mPrefix + key, value);
		}

		public void putInt(String key, int value) {
			if (!mEditable) {
				throw new IllegalStateException("StateBundle is not editable");
			}
			mPrefEditor.putInt(mPrefix + key, value);
		}

		public void putString(String key, String value) {
			if (!mEditable) {
				throw new IllegalStateException("StateBundle is not editable");
			}
			mPrefEditor.putString(mPrefix + key, value);
		}

		public void commit() {
			if (!mEditable) {
				throw new IllegalStateException("StateBundle is not editable");
			}
			mPrefEditor.commit();
		}

	}

}
