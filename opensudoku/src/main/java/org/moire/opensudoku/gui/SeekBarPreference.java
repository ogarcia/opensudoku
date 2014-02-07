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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import org.moire.opensudoku.R;

/**
 * A {@link Preference} that allows for integer
 * input.
 * <p/>
 * It is a subclass of {@link DialogPreference} and shows the {@link SeekBar}
 * in a dialog.
 * <p/>
 * <code>SeekBarPreference</code> differs slightly from <code>SeekBar</code>:
 * <code>SeekBar</code> does not allow minimum other than 0. To overcome this, <code>SeekBarPreference</code>
 * adds mininum field ({@link #getMin()}, {@link #setMin(int)}) and tracks value instead of progress
 * ({@link #getValue()}, {@link #setValue(int)}).
 * <p/>
 */
public class SeekBarPreference extends DialogPreference {
	/**
	 * The edit text shown in the dialog.
	 */
	private SeekBar mSeekBar;
	private TextView mValueLabel;

	private int mMin;
	private int mMax;
	private int mValue;
	private String mValueFormat;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.preference_dialog_seek_bar);

		mSeekBar = new SeekBar(context, attrs);
		// Give it an ID so it can be saved/restored
		mSeekBar.setId(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

		TypedArray a =
				context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
		setMin(a.getInt(R.styleable.SeekBarPreference_min, mMin));
		setMax(a.getInt(R.styleable.SeekBarPreference_max, mMax));
		setValue(a.getInt(R.styleable.SeekBarPreference_value, mValue));
		setValueFormat(a.getString(R.styleable.SeekBarPreference_valueFormat));

		a.recycle();
	}

	public SeekBarPreference(Context context) {
		this(context, null);
	}

	/**
	 * Sets minimal value which can be set by this preference object.
	 *
	 * @param min
	 */
	public void setMin(int min) {
		mMin = min;
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setProgress(mMin);
	}

	/**
	 * Returns minimal value which can be set by this preference object.
	 *
	 * @return
	 */
	public int getMin() {
		return mMin;
	}

	/**
	 * Sets maximal value which can be set by this preference object.
	 *
	 * @param max
	 */
	public void setMax(int max) {
		mMax = max;
		mSeekBar.setMax(mMax - mMin);
		mSeekBar.setProgress(mMin);
	}

	/**
	 * Returns maximal value which can be set by this preference object.
	 *
	 * @return
	 */
	public int getMax() {
		return mMax;
	}

	/**
	 * Saves the value to the {@link SharedPreferences}.
	 */
	public void setValue(int value) {
		final boolean wasBlocking = shouldDisableDependents();

		if (value > mMax) {
			mValue = mMax;
		} else if (value < mMin) {
			mValue = mMin;
		} else {
			mValue = value;
		}

		persistInt(value);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}

	/**
	 * Gets the value from the {@link SharedPreferences}.
	 *
	 * @return The current preference value.
	 */
	public int getValue() {
		return mValue;
	}

	public void setValueFormat(String valueFormat) {
		mValueFormat = valueFormat;
	}

	public String getValueFormat() {
		return mValueFormat;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		mValueLabel = (TextView) view.findViewById(R.id.value);

		SeekBar seekBar = mSeekBar;
		seekBar.setProgress(getValue() - mMin);
		updateValueLabel(seekBar.getProgress());


		ViewParent oldParent = seekBar.getParent();
		if (oldParent != view) {
			if (oldParent != null) {
				((ViewGroup) oldParent).removeView(seekBar);
			}
			onAddSeekBarToDialogView(view, seekBar);
		}
	}

	/**
	 * Adds the SeekBar widget of this preference to the dialog's view.
	 *
	 * @param dialogView The dialog view.
	 */
	protected void onAddSeekBarToDialogView(View dialogView, SeekBar seekBar) {
		ViewGroup container = (ViewGroup) dialogView
				.findViewById(R.id.seek_bar_container);
		if (container != null) {
			container.addView(seekBar, ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
	}

	private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			updateValueLabel(progress);
		}

	};

	private void updateValueLabel(int progress) {
		if (mValueLabel != null) {
			int value = progress + mMin;
			if (mValueFormat != null && mValueFormat != "") {
				mValueLabel.setText(String.format(mValueFormat, value));
			} else {
				mValueLabel.setText(String.valueOf(value));
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			int progress = mSeekBar.getProgress() + mMin;
			if (callChangeListener(progress)) {
				setValue(progress);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		int defValue = mMin;
		if (defaultValue != null) {
			defValue = Integer.parseInt(defaultValue.toString());
		}
		setValue(restoreValue ? getPersistedInt(mValue) : defValue);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.value = getValue();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setValue(myState.value);
	}

	private static class SavedState extends BaseSavedState {
		int value;

		public SavedState(Parcel source) {
			super(source);
			value = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(value);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

}