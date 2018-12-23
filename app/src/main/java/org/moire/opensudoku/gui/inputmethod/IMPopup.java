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

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.gui.inputmethod.IMPopupDialog.OnNoteEditListener;
import org.moire.opensudoku.gui.inputmethod.IMPopupDialog.OnNumberEditListener;

import java.util.Map;

public class IMPopup extends InputMethod {

    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;

    private IMPopupDialog mEditCellDialog;
    private Cell mSelectedCell;

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    /**
     * If set to true, buttons for numbers, which occur in {@link CellCollection}
     * more than {@link CellCollection#SUDOKU_SIZE}-times, will be highlighted.
     *
     * @param highlightCompletedValues
     */
    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    private void ensureEditCellDialog() {
        if (mEditCellDialog == null) {
            mEditCellDialog = new IMPopupDialog(mContext);
            mEditCellDialog.setOnNumberEditListener(mOnNumberEditListener);
            mEditCellDialog.setOnNoteEditListener(mOnNoteEditListener);
            mEditCellDialog.setOnDismissListener(mOnPopupDismissedListener);
        }

    }

    @Override
    protected void onActivated() {
        mBoard.setAutoHideTouchedCellHint(false);
    }

    @Override
    protected void onDeactivated() {
        mBoard.setAutoHideTouchedCellHint(true);
    }

    @Override
    protected void onCellTapped(Cell cell) {
        mSelectedCell = cell;
        if (cell.isEditable()) {
            ensureEditCellDialog();

            mEditCellDialog.resetButtons();
            mEditCellDialog.updateNumber(cell.getValue());
            mEditCellDialog.updateNote(cell.getNote().getNotedNumbers());

            Map<Integer, Integer> valuesUseCount = null;
            if (mHighlightCompletedValues || mShowNumberTotals)
                valuesUseCount = mGame.getCells().getValuesUseCount();

            if (mHighlightCompletedValues) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    if (entry.getValue() >= CellCollection.SUDOKU_SIZE) {
                        mEditCellDialog.highlightNumber(entry.getKey());
                    }
                }
            }

            if (mShowNumberTotals) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    mEditCellDialog.setValueCount(entry.getKey(), entry.getValue());
                }
            }
            mEditCellDialog.show();
        } else {
            mBoard.hideTouchedCellHint();
        }
    }

    @Override
    protected void onPause() {
        // release dialog resource (otherwise WindowLeaked exception is logged)
        if (mEditCellDialog != null) {
            mEditCellDialog.cancel();
        }
    }

    @Override
    public int getNameResID() {
        return R.string.popup;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_popup_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.popup_abbr);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.im_popup, null);
    }

    /**
     * Occurs when user selects number in EditCellDialog.
     */
    private OnNumberEditListener mOnNumberEditListener = new OnNumberEditListener() {
        @Override
        public boolean onNumberEdit(int number) {
            if (number != -1 && mSelectedCell != null) {
                mGame.setCellValue(mSelectedCell, number);
            }
            return true;
        }
    };

    /**
     * Occurs when user edits note in EditCellDialog
     */
    private OnNoteEditListener mOnNoteEditListener = new OnNoteEditListener() {
        @Override
        public boolean onNoteEdit(Integer[] numbers) {
            if (mSelectedCell != null) {
                mGame.setCellNote(mSelectedCell, CellNote.fromIntArray(numbers));
            }
            return true;
        }
    };

    /**
     * Occurs when popup dialog is closed.
     */
    private OnDismissListener mOnPopupDismissedListener = dialog -> mBoard.hideTouchedCellHint();

}
