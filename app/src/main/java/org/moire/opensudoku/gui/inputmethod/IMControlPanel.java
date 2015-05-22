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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.SudokuBoardView.OnCellSelectedListener;
import org.moire.opensudoku.gui.SudokuBoardView.OnCellTappedListener;

/**
 * @author romario
 */
public class IMControlPanel extends LinearLayout {
	public static final int INPUT_METHOD_POPUP = 0;
	public static final int INPUT_METHOD_SINGLE_NUMBER = 1;
	public static final int INPUT_METHOD_NUMPAD = 2;

	private Context mContext;
	private SudokuBoardView mBoard;
	private SudokuGame mGame;
	private HintsQueue mHintsQueue;

	private List<InputMethod> mInputMethods = new ArrayList<InputMethod>();
	private int mActiveMethodIndex = -1;

	public IMControlPanel(Context context) {
		super(context);
		mContext = context;
	}

	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void initialize(SudokuBoardView board, SudokuGame game, HintsQueue hintsQueue) {
		mBoard = board;
		mBoard.setOnCellTappedListener(mOnCellTapListener);
		mBoard.setOnCellSelectedListener(mOnCellSelected);

		mGame = game;
		mHintsQueue = hintsQueue;

		createInputMethods();
	}

	/**
	 * Activates first enabled input method. If such method does not exists, nothing
	 * happens.
	 */
	public void activateFirstInputMethod() {
		ensureInputMethods();
		if (mActiveMethodIndex == -1 || !mInputMethods.get(mActiveMethodIndex).isEnabled()) {
			activateInputMethod(0);
		}

	}

	/**
	 * Activates given input method (see INPUT_METHOD_* constants). If the given method is
	 * not enabled, activates first available method after this method.
	 *
	 * @param methodID ID of method input to activate.
	 * @return
	 */
	public void activateInputMethod(int methodID) {
		if (methodID < -1 || methodID >= mInputMethods.size()) {
			throw new IllegalArgumentException(String.format("Invalid method id: %s.", methodID));
		}

		ensureInputMethods();

		if (mActiveMethodIndex != -1) {
			mInputMethods.get(mActiveMethodIndex).deactivate();
		}

		boolean idFound = false;
		int id = methodID;
		int numOfCycles = 0;

		if (id != -1) {
			while (!idFound && numOfCycles <= mInputMethods.size()) {
				if (mInputMethods.get(id).isEnabled()) {
					ensureControlPanel(id);
					idFound = true;
					break;
				}

				id++;
				if (id == mInputMethods.size()) {
					id = 0;
				}
				numOfCycles++;
			}
		}

		if (!idFound) {
			id = -1;
		}

		for (int i = 0; i < mInputMethods.size(); i++) {
			InputMethod im = mInputMethods.get(i);
			if (im.isInputMethodViewCreated()) {
				im.getInputMethodView().setVisibility(i == id ? View.VISIBLE : View.GONE);
			}
		}

		mActiveMethodIndex = id;
		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.activate();

			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint(activeMethod.getInputMethodName(), activeMethod.getNameResID(), activeMethod.getHelpResID());
			}
		}
	}

	public void activateNextInputMethod() {
		ensureInputMethods();

		int id = mActiveMethodIndex + 1;
		if (id >= mInputMethods.size()) {
			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint("thatIsAll", R.string.that_is_all, R.string.im_disable_modes_hint);
			}
			id = 0;
		}
		activateInputMethod(id);
	}

	/**
	 * Returns input method object by its ID (see INPUT_METHOD_* constants).
	 *
	 * @param methodId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends InputMethod> T getInputMethod(int methodId) {
		ensureInputMethods();

		return (T) mInputMethods.get(methodId);
	}

	public List<InputMethod> getInputMethods() {
		return Collections.unmodifiableList(mInputMethods);
	}

	public int getActiveMethodIndex() {
		return mActiveMethodIndex;
	}

	public void showHelpForActiveMethod() {
		ensureInputMethods();

		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.activate();

			mHintsQueue.showHint(activeMethod.getNameResID(), activeMethod.getHelpResID());
		}
	}

	// TODO: Is this really necessary? 

	/**
	 * This should be called when activity is paused (so Input Methods can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		for (InputMethod im : mInputMethods) {
			im.pause();
		}
	}

	/**
	 * Ensures that all input method objects are created.
	 */
	private void ensureInputMethods() {
		if (mInputMethods.size() == 0) {
			throw new IllegalStateException("Input methods are not created yet. Call initialize() first.");
		}

	}

	private void createInputMethods() {
		if (mInputMethods.size() == 0) {
			addInputMethod(INPUT_METHOD_POPUP, new IMPopup());
			addInputMethod(INPUT_METHOD_SINGLE_NUMBER, new IMSingleNumber());
			addInputMethod(INPUT_METHOD_NUMPAD, new IMNumpad());
		}
	}

	private void addInputMethod(int methodIndex, InputMethod im) {
		im.initialize(mContext, this, mGame, mBoard, mHintsQueue);
		mInputMethods.add(methodIndex, im);
	}

	/**
	 * Ensures that control panel for given input method is created.
	 *
	 * @param methodID
	 */
	private void ensureControlPanel(int methodID) {
		InputMethod im = mInputMethods.get(methodID);
		if (!im.isInputMethodViewCreated()) {
			View controlPanel = im.getInputMethodView();
			Button switchModeButton = (Button) controlPanel.findViewById(R.id.switch_input_mode);
			switchModeButton.setOnClickListener(mSwitchModeListener);
			this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	}

	private OnCellTappedListener mOnCellTapListener = new OnCellTappedListener() {
		@Override
		public void onCellTapped(Cell cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellTapped(cell);
			}
		}
	};

	private OnCellSelectedListener mOnCellSelected = new OnCellSelectedListener() {
		@Override
		public void onCellSelected(Cell cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellSelected(cell);
			}
		}
	};

	private OnClickListener mSwitchModeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			activateNextInputMethod();
		}
	};

//    /**
//     * Used to save / restore state of control panel.
//     */
//    private static class SavedState extends BaseSavedState {
//    	private final int mActiveMethodIndex;
//        private final Bundle mInputMethodsState;
//    	
//    	private SavedState(Parcelable superState, int activeMethodIndex, List<InputMethod> inputMethods) {
//            super(superState);
//            mActiveMethodIndex = activeMethodIndex;
//            
//            mInputMethodsState = new Bundle();
//            for (InputMethod im : inputMethods) {
//            	im.onSaveInstanceState(mInputMethodsState);
//            }
//        }
//        
//        private SavedState(Parcel in) {
//            super(in);
//            mActiveMethodIndex = in.readInt();
//            mInputMethodsState = in.readBundle();
//        }
//
//        public int getActiveMethodIndex() {
//            return mActiveMethodIndex;
//        }
//        
//        public void restoreInputMethodsState(List<InputMethod> inputMethods) {
//        	for (InputMethod im : inputMethods) {
//        		im.onRestoreInstanceState(mInputMethodsState);
//        	}
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            super.writeToParcel(dest, flags);
//            dest.writeInt(mActiveMethodIndex);
//            dest.writeBundle(mInputMethodsState);
//        }
//
//        public static final Parcelable.Creator<SavedState> CREATOR
//                = new Creator<SavedState>() {
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//        };
//    	
//    }


}
