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

package org.moire.opensudoku.gui.inputmethod;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.ThemeUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IMPopupDialog extends Dialog {

    private Context mContext;
    private LayoutInflater mInflater;
    private TabHost mTabHost;

    // buttons from "Select number" tab
    private Map<Integer, Button> mNumberButtons = new HashMap<>();
    // buttons from "Edit note" tab
    private Map<Integer, ToggleButton> mNoteNumberButtons = new HashMap<>();

    // selected number on "Select number" tab (0 if nothing is selected).
    private int mSelectedNumber;
    // selected numbers on "Edit note" tab
    private Set<Integer> mNoteSelectedNumbers = new HashSet<>();

    private OnNumberEditListener mOnNumberEditListener;
    private OnNoteEditListener mOnNoteEditListener;

    public IMPopupDialog(Context context) {
        super(context);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTabHost = createTabView();

        setContentView(mTabHost);
    }

    /**
     * Registers a callback to be invoked when number is selected.
     *
     * @param l
     */
    public void setOnNumberEditListener(OnNumberEditListener l) {
        mOnNumberEditListener = l;
    }

    /**
     * Register a callback to be invoked when note is edited.
     *
     * @param l
     */
    public void setOnNoteEditListener(OnNoteEditListener l) {
        mOnNoteEditListener = l;
    }

    public void resetButtons() {
        for (Map.Entry<Integer, ToggleButton> entry : mNoteNumberButtons.entrySet()) {
            entry.getValue().setText("" + entry.getKey());
            ThemeUtils.applyIMButtonStateToView(entry.getValue(), ThemeUtils.IMButtonStyle.DEFAULT);
        }

        for (Map.Entry<Integer, Button> entry : mNumberButtons.entrySet()) {
            ThemeUtils.applyIMButtonStateToView(entry.getValue(), ThemeUtils.IMButtonStyle.DEFAULT);
        }
    }

    // TODO: vsude jinde pouzivam misto number value
    public void updateNumber(Integer number) {
        mSelectedNumber = number;
        for (Map.Entry<Integer, Button> entry : mNumberButtons.entrySet()) {
            Button b = entry.getValue();
            if (entry.getKey().equals(mSelectedNumber)) {
                ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
            } else {
                ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
            }
        }
    }

    /**
     * Updates selected numbers in note.
     *
     * @param numbers
     */
    public void updateNote(Collection<Integer> numbers) {
        mNoteSelectedNumbers = new HashSet<>();

        if (numbers != null) {
            mNoteSelectedNumbers.addAll(numbers);
        }

        ToggleButton toggleButton;
        for (Integer number : mNoteNumberButtons.keySet()) {
            toggleButton = mNoteNumberButtons.get(number);
            toggleButton.setChecked(mNoteSelectedNumbers.contains(number));
            if (toggleButton.isChecked()) {
                ThemeUtils.applyIMButtonStateToView(toggleButton, ThemeUtils.IMButtonStyle.ACCENT);
            }
        }
    }

    public void highlightNumber(int number) {

        Button b = mNumberButtons.get(number);
        if (number == mSelectedNumber) {
            // Set color of completed and selected number
            ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
        } else {
            ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
        }
    }

    public void setValueCount(int number, int count) {
        mNumberButtons.get(number).setText(number + " (" + count + ")");
    }

    /**
     * Creates view with two tabs, first for number in cell selection, second for
     * note editing.
     *
     * @return
     */
    private TabHost createTabView() {
        TabHost tabHost = new TabHost(mContext, null);
        tabHost.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        //tabHost.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        //linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TabWidget tabWidget = new TabWidget(mContext);
        tabWidget.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        tabWidget.setId(android.R.id.tabs);

        FrameLayout frameLayout = new FrameLayout(mContext);
        frameLayout.setId(android.R.id.tabcontent);

        linearLayout.addView(tabWidget);
        linearLayout.addView(frameLayout);
        tabHost.addView(linearLayout);

        tabHost.setup();

        final View editNumberView = createEditNumberView();
        final View editNoteView = createEditNoteView();

        tabHost.addTab(tabHost.newTabSpec("number")
                .setIndicator(mContext.getString(R.string.select_number))
                .setContent(tag -> editNumberView));
        tabHost.addTab(tabHost.newTabSpec("note")
                .setIndicator(mContext.getString(R.string.edit_note))
                .setContent(tag -> editNoteView));

        return tabHost;
    }

    /**
     * Creates view for number in cell editing.
     *
     * @return
     */
    private View createEditNumberView() {
        View v = mInflater.inflate(R.layout.im_popup_edit_value, null);

        mNumberButtons.put(1, v.findViewById(R.id.button_1));
        mNumberButtons.put(2, v.findViewById(R.id.button_2));
        mNumberButtons.put(3, v.findViewById(R.id.button_3));
        mNumberButtons.put(4, v.findViewById(R.id.button_4));
        mNumberButtons.put(5, v.findViewById(R.id.button_5));
        mNumberButtons.put(6, v.findViewById(R.id.button_6));
        mNumberButtons.put(7, v.findViewById(R.id.button_7));
        mNumberButtons.put(8, v.findViewById(R.id.button_8));
        mNumberButtons.put(9, v.findViewById(R.id.button_9));

        for (Integer num : mNumberButtons.keySet()) {
            Button b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(editNumberButtonClickListener);
        }

        Button closeButton = v.findViewById(R.id.button_close);
        closeButton.setOnClickListener(closeButtonListener);
        Button clearButton = v.findViewById(R.id.button_clear);
        clearButton.setOnClickListener(clearButtonListener);

        return v;
    }


    /**
     * Creates view for note editing.
     *
     * @return
     */
    private View createEditNoteView() {
        View v = mInflater.inflate(R.layout.im_popup_edit_note, null);

        mNoteNumberButtons.put(1, v.findViewById(R.id.button_1));
        mNoteNumberButtons.put(2, v.findViewById(R.id.button_2));
        mNoteNumberButtons.put(3, v.findViewById(R.id.button_3));
        mNoteNumberButtons.put(4, v.findViewById(R.id.button_4));
        mNoteNumberButtons.put(5, v.findViewById(R.id.button_5));
        mNoteNumberButtons.put(6, v.findViewById(R.id.button_6));
        mNoteNumberButtons.put(7, v.findViewById(R.id.button_7));
        mNoteNumberButtons.put(8, v.findViewById(R.id.button_8));
        mNoteNumberButtons.put(9, v.findViewById(R.id.button_9));

        for (Integer num : mNoteNumberButtons.keySet()) {
            ToggleButton b = mNoteNumberButtons.get(num);
            b.setTag(num);
            b.setOnCheckedChangeListener(editNoteCheckedChangeListener);
        }

        Button closeButton = v.findViewById(R.id.button_close);
        closeButton.setOnClickListener(closeButtonListener);
        Button clearButton = v.findViewById(R.id.button_clear);
        clearButton.setOnClickListener(clearButtonListener);

        return v;
    }

    /**
     * Occurs when user selects number in "Select number" tab.
     */
    private View.OnClickListener editNumberButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer number = (Integer) v.getTag();

            if (mOnNumberEditListener != null) {
                mOnNumberEditListener.onNumberEdit(number);
            }

            dismiss();
        }
    };

    /**
     * Occurs when user checks or unchecks number in "Edit note" tab.
     */
    private OnCheckedChangeListener editNoteCheckedChangeListener = (buttonView, isChecked) -> {
        Integer number = (Integer) buttonView.getTag();
        if (isChecked) {
            mNoteSelectedNumbers.add(number);
        } else {
            mNoteSelectedNumbers.remove(number);
        }
        ThemeUtils.applyIMButtonStateToView(buttonView, isChecked ? ThemeUtils.IMButtonStyle.ACCENT : ThemeUtils.IMButtonStyle.DEFAULT);
    };

    /**
     * Occurs when user presses "Clear" button.
     */
    private View.OnClickListener clearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String currentTab = mTabHost.getCurrentTabTag();

            if (currentTab.equals("number")) {
                if (mOnNumberEditListener != null) {
                    mOnNumberEditListener.onNumberEdit(0); // 0 as clear
                }
                dismiss();
            } else {
                for (ToggleButton b : mNoteNumberButtons.values()) {
                    b.setChecked(false);
                    mNoteSelectedNumbers.remove(b.getTag());
                }
            }
        }
    };

    /**
     * Occurs when user presses "Close" button.
     */
    private View.OnClickListener closeButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mOnNoteEditListener != null) {
                Integer[] numbers = new Integer[mNoteSelectedNumbers.size()];
                mOnNoteEditListener.onNoteEdit(mNoteSelectedNumbers.toArray(numbers));
            }
            dismiss();
        }
    };

    /**
     * Interface definition for a callback to be invoked, when user selects number, which
     * should be entered in the sudoku cell.
     *
     * @author romario
     */
    public interface OnNumberEditListener {
        boolean onNumberEdit(int number);
    }

    /**
     * Interface definition for a callback to be invoked, when user selects new note
     * content.
     *
     * @author romario
     */
    public interface OnNoteEditListener {
        boolean onNoteEdit(Integer[] number);
    }

}
