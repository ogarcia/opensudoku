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

import java.util.Collection;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.game.CellCollection.OnChangeListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Sudoku board widget.
 *
 * @author romario
 */
public class SudokuBoardView extends View {

	public static final int DEFAULT_BOARD_SIZE = 100;

	/**
	 * "Color not set" value. (In relation to {@link Color}, it is in fact black color with
	 * alpha channel set to 0 => that means it is completely transparent).
	 */
	private static final int NO_COLOR = 0;

	private float mCellWidth;
	private float mCellHeight;

	private Cell mTouchedCell;
	// TODO: should I synchronize access to mSelectedCell?
	private Cell mSelectedCell;
	private boolean mReadonly = false;
	private boolean mHighlightWrongVals = true;
	private boolean mHighlightTouchedCell = true;
	private boolean mAutoHideTouchedCellHint = true;

	private SudokuGame mGame;
	private CellCollection mCells;

	private OnCellTappedListener mOnCellTappedListener;
	private OnCellSelectedListener mOnCellSelectedListener;

	private Paint mLinePaint;
	private Paint mSectorLinePaint;
	private Paint mCellValuePaint;
	private Paint mCellValueReadonlyPaint;
	private Paint mCellNotePaint;
	private int mNumberLeft;
	private int mNumberTop;
	private float mNoteTop;
	private int mSectorLineWidth;
	private Paint mBackgroundColorSecondary;
	private Paint mBackgroundColorReadOnly;
	private Paint mBackgroundColorTouched;
	private Paint mBackgroundColorSelected;

	private Paint mCellValueInvalidPaint;

	public SudokuBoardView(Context context) {
		this(context, null);
	}

	//	public SudokuBoardView(Context context, AttributeSet attrs) {
	//		this(context, attrs, R.attr.sudokuBoardViewStyle);
	//	}

	// TODO: do I need an defStyle?
	public SudokuBoardView(Context context, AttributeSet attrs/*, int defStyle*/) {
		super(context, attrs/*, defStyle*/);

		setFocusable(true);
		setFocusableInTouchMode(true);

		mLinePaint = new Paint();
		mSectorLinePaint = new Paint();
		mCellValuePaint = new Paint();
		mCellValueReadonlyPaint = new Paint();
		mCellValueInvalidPaint = new Paint();
		mCellNotePaint = new Paint();
		mBackgroundColorSecondary = new Paint();
		mBackgroundColorReadOnly = new Paint();
		mBackgroundColorTouched = new Paint();
		mBackgroundColorSelected = new Paint();

		mCellValuePaint.setAntiAlias(true);
		mCellValueReadonlyPaint.setAntiAlias(true);
		mCellValueInvalidPaint.setAntiAlias(true);
		mCellNotePaint.setAntiAlias(true);
		mCellValueInvalidPaint.setColor(Color.RED);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudokuBoardView/*, defStyle, 0*/);

		setLineColor(a.getColor(R.styleable.SudokuBoardView_lineColor, Color.BLACK));
		setSectorLineColor(a.getColor(R.styleable.SudokuBoardView_sectorLineColor, Color.BLACK));
		setTextColor(a.getColor(R.styleable.SudokuBoardView_textColor, Color.BLACK));
		setTextColorReadOnly(a.getColor(R.styleable.SudokuBoardView_textColorReadOnly, Color.BLACK));
		setTextColorNote(a.getColor(R.styleable.SudokuBoardView_textColorNote, Color.BLACK));
		setBackgroundColor(a.getColor(R.styleable.SudokuBoardView_backgroundColor, Color.WHITE));
		setBackgroundColorSecondary(a.getColor(R.styleable.SudokuBoardView_backgroundColorSecondary, NO_COLOR));
		setBackgroundColorReadOnly(a.getColor(R.styleable.SudokuBoardView_backgroundColorReadOnly, NO_COLOR));
		setBackgroundColorTouched(a.getColor(R.styleable.SudokuBoardView_backgroundColorTouched, Color.rgb(50, 50, 255)));
		setBackgroundColorSelected(a.getColor(R.styleable.SudokuBoardView_backgroundColorSelected, Color.YELLOW));

		a.recycle();
	}

	public int getLineColor() {
		return mLinePaint.getColor();
	}

	public void setLineColor(int color) {
		mLinePaint.setColor(color);
	}

	public int getSectorLineColor() {
		return mSectorLinePaint.getColor();
	}

	public void setSectorLineColor(int color) {
		mSectorLinePaint.setColor(color);
	}

	public int getTextColor() {
		return mCellValuePaint.getColor();
	}

	public void setTextColor(int color) {
		mCellValuePaint.setColor(color);
	}

	public int getTextColorReadOnly() {
		return mCellValueReadonlyPaint.getColor();
	}

	public void setTextColorReadOnly(int color) {
		mCellValueReadonlyPaint.setColor(color);
	}

	public int getTextColorNote() {
		return mCellNotePaint.getColor();
	}

	public void setTextColorNote(int color) {
		mCellNotePaint.setColor(color);
	}

	public int getBackgroundColorSecondary() {
		return mBackgroundColorSecondary.getColor();
	}

	public void setBackgroundColorSecondary(int color) {
		mBackgroundColorSecondary.setColor(color);
	}

	public int getBackgroundColorReadOnly() {
		return mBackgroundColorReadOnly.getColor();
	}

	public void setBackgroundColorReadOnly(int color) {
		mBackgroundColorReadOnly.setColor(color);
	}

	public int getBackgroundColorTouched() {
		return mBackgroundColorTouched.getColor();
	}

	public void setBackgroundColorTouched(int color) {
		mBackgroundColorTouched.setColor(color);
		mBackgroundColorTouched.setAlpha(100);
	}

	public int getBackgroundColorSelected() {
		return mBackgroundColorSelected.getColor();
	}

	public void setBackgroundColorSelected(int color) {
		mBackgroundColorSelected.setColor(color);
		mBackgroundColorSelected.setAlpha(100);
	}

	public void setGame(SudokuGame game) {
		mGame = game;
		setCells(game.getCells());
	}

	public void setCells(CellCollection cells) {
		mCells = cells;

		if (mCells != null) {
			if (!mReadonly) {
				mSelectedCell = mCells.getCell(0, 0); // first cell will be selected by default
				onCellSelected(mSelectedCell);
			}

			mCells.addOnChangeListener(new OnChangeListener() {
				@Override
				public void onChange() {
					postInvalidate();
				}
			});
		}

		postInvalidate();
	}

	public CellCollection getCells() {
		return mCells;
	}

	public Cell getSelectedCell() {
		return mSelectedCell;
	}

	public void setReadOnly(boolean readonly) {
		mReadonly = readonly;
		postInvalidate();
	}

	public boolean isReadOnly() {
		return mReadonly;
	}

	public void setHighlightWrongVals(boolean highlightWrongVals) {
		mHighlightWrongVals = highlightWrongVals;
		postInvalidate();
	}

	public boolean getHighlightWrongVals() {
		return mHighlightWrongVals;
	}

	public void setHighlightTouchedCell(boolean highlightTouchedCell) {
		mHighlightTouchedCell = highlightTouchedCell;
	}

	public boolean getHighlightTouchedCell() {
		return mHighlightTouchedCell;
	}

	public void setAutoHideTouchedCellHint(boolean autoHideTouchedCellHint) {
		mAutoHideTouchedCellHint = autoHideTouchedCellHint;
	}

	public boolean getAutoHideTouchedCellHint() {
		return mAutoHideTouchedCellHint;
	}

	/**
	 * Registers callback which will be invoked when user taps the cell.
	 *
	 * @param l
	 */
	public void setOnCellTappedListener(OnCellTappedListener l) {
		mOnCellTappedListener = l;
	}

	protected void onCellTapped(Cell cell) {
		if (mOnCellTappedListener != null) {
			mOnCellTappedListener.onCellTapped(cell);
		}
	}

	/**
	 * Registers callback which will be invoked when cell is selected. Cell selection
	 * can change without user interaction.
	 *
	 * @param l
	 */
	public void setOnCellSelectedListener(OnCellSelectedListener l) {
		mOnCellSelectedListener = l;
	}

	public void hideTouchedCellHint() {
		mTouchedCell = null;
		postInvalidate();
	}


	protected void onCellSelected(Cell cell) {
		if (mOnCellSelectedListener != null) {
			mOnCellSelectedListener.onCellSelected(cell);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);


//        Log.d(TAG, "widthMode=" + getMeasureSpecModeString(widthMode));
//        Log.d(TAG, "widthSize=" + widthSize);
//        Log.d(TAG, "heightMode=" + getMeasureSpecModeString(heightMode));
//        Log.d(TAG, "heightSize=" + heightSize);

		int width = -1, height = -1;
		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = DEFAULT_BOARD_SIZE;
			if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
				width = widthSize;
			}
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = DEFAULT_BOARD_SIZE;
			if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
				height = heightSize;
			}
		}

		if (widthMode != MeasureSpec.EXACTLY) {
			width = height;
		}

		if (heightMode != MeasureSpec.EXACTLY) {
			height = width;
		}

		if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
			width = widthSize;
		}
		if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
			height = heightSize;
		}

		mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
		mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

		setMeasuredDimension(width, height);

		float cellTextSize = mCellHeight * 0.75f;
		mCellValuePaint.setTextSize(cellTextSize);
		mCellValueReadonlyPaint.setTextSize(cellTextSize);
		mCellValueInvalidPaint.setTextSize(cellTextSize);
		mCellNotePaint.setTextSize(mCellHeight / 3.0f);
		// compute offsets in each cell to center the rendered number
		mNumberLeft = (int) ((mCellWidth - mCellValuePaint.measureText("9")) / 2);
		mNumberTop = (int) ((mCellHeight - mCellValuePaint.getTextSize()) / 2);

		// add some offset because in some resolutions notes are cut-off in the top
		mNoteTop = mCellHeight / 50.0f;

		computeSectorLineWidth(width, height);
	}

	private void computeSectorLineWidth(int widthInPx, int heightInPx) {
		int sizeInPx = widthInPx < heightInPx ? widthInPx : heightInPx;
		float dipScale = getContext().getResources().getDisplayMetrics().density;
		float sizeInDip = sizeInPx / dipScale;

		float sectorLineWidthInDip = 2.0f;

		if (sizeInDip > 150) {
			sectorLineWidthInDip = 3.0f;
		}

		mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// some notes:
		// Drawable has its own draw() method that takes your Canvas as an arguement

		// TODO: I don't get this, why do I need to substract padding only from one side?
		int width = getWidth() - getPaddingRight();
		int height = getHeight() - getPaddingBottom();

		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();

		// draw secondary background
		if (mBackgroundColorSecondary.getColor() != NO_COLOR) {
			canvas.drawRect(3 * mCellWidth, 0, 6 * mCellWidth, 3 * mCellWidth, mBackgroundColorSecondary);
			canvas.drawRect(0, 3 * mCellWidth, 3 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
			canvas.drawRect(6 * mCellWidth, 3 * mCellWidth, 9 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
			canvas.drawRect(3 * mCellWidth, 6 * mCellWidth, 6 * mCellWidth, 9 * mCellWidth, mBackgroundColorSecondary);
		}

		// draw cells
		int cellLeft, cellTop;
		if (mCells != null) {

			boolean hasBackgroundColorReadOnly = mBackgroundColorReadOnly.getColor() != NO_COLOR;

			float numberAscent = mCellValuePaint.ascent();
			float noteAscent = mCellNotePaint.ascent();
			float noteWidth = mCellWidth / 3f;
			for (int row = 0; row < 9; row++) {
				for (int col = 0; col < 9; col++) {
					Cell cell = mCells.getCell(row, col);

					cellLeft = Math.round((col * mCellWidth) + paddingLeft);
					cellTop = Math.round((row * mCellHeight) + paddingTop);

					// draw read-only field background
					if (!cell.isEditable() && hasBackgroundColorReadOnly) {
						if (mBackgroundColorReadOnly.getColor() != NO_COLOR) {
							canvas.drawRect(
									cellLeft, cellTop,
									cellLeft + mCellWidth, cellTop + mCellHeight,
									mBackgroundColorReadOnly);
						}
					}

					// draw cell Text
					int value = cell.getValue();
					if (value != 0) {
						Paint cellValuePaint = cell.isEditable() ? mCellValuePaint : mCellValueReadonlyPaint;

						if (mHighlightWrongVals && !cell.isValid()) {
							cellValuePaint = mCellValueInvalidPaint;
						}
						canvas.drawText(Integer.toString(value),
								cellLeft + mNumberLeft,
								cellTop + mNumberTop - numberAscent,
								cellValuePaint);
					} else {
						if (!cell.getNote().isEmpty()) {
							Collection<Integer> numbers = cell.getNote().getNotedNumbers();
							for (Integer number : numbers) {
								int n = number - 1;
								int c = n % 3;
								int r = n / 3;
								//canvas.drawText(Integer.toString(number), cellLeft + c*noteWidth + 2, cellTop + noteAscent + r*noteWidth - 1, mNotePaint);
								canvas.drawText(Integer.toString(number), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, mCellNotePaint);
							}
						}
					}


				}
			}

			// highlight selected cell
			if (!mReadonly && mSelectedCell != null) {
				cellLeft = Math.round(mSelectedCell.getColumnIndex() * mCellWidth) + paddingLeft;
				cellTop = Math.round(mSelectedCell.getRowIndex() * mCellHeight) + paddingTop;
				canvas.drawRect(
						cellLeft, cellTop,
						cellLeft + mCellWidth, cellTop + mCellHeight,
						mBackgroundColorSelected);
			}

			// visually highlight cell under the finger (to cope with touch screen
			// imprecision)
			if (mHighlightTouchedCell && mTouchedCell != null) {
				cellLeft = Math.round(mTouchedCell.getColumnIndex() * mCellWidth) + paddingLeft;
				cellTop = Math.round(mTouchedCell.getRowIndex() * mCellHeight) + paddingTop;
				canvas.drawRect(
						cellLeft, paddingTop,
						cellLeft + mCellWidth, height,
						mBackgroundColorTouched);
				canvas.drawRect(
						paddingLeft, cellTop,
						width, cellTop + mCellHeight,
						mBackgroundColorTouched);
			}

		}

		// draw vertical lines
		for (int c = 0; c <= 9; c++) {
			float x = (c * mCellWidth) + paddingLeft;
			canvas.drawLine(x, paddingTop, x, height, mLinePaint);
		}

		// draw horizontal lines
		for (int r = 0; r <= 9; r++) {
			float y = r * mCellHeight + paddingTop;
			canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
		}

		int sectorLineWidth1 = mSectorLineWidth / 2;
		int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

		// draw sector (thick) lines
		for (int c = 0; c <= 9; c = c + 3) {
			float x = (c * mCellWidth) + paddingLeft;
			canvas.drawRect(x - sectorLineWidth1, paddingTop, x + sectorLineWidth2, height, mSectorLinePaint);
		}

		for (int r = 0; r <= 9; r = r + 3) {
			float y = r * mCellHeight + paddingTop;
			canvas.drawRect(paddingLeft, y - sectorLineWidth1, width, y + sectorLineWidth2, mSectorLinePaint);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!mReadonly) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					mTouchedCell = getCellAtPoint(x, y);
					break;
				case MotionEvent.ACTION_UP:
					mSelectedCell = getCellAtPoint(x, y);
					invalidate(); // selected cell has changed, update board as soon as you can

					if (mSelectedCell != null) {
						onCellTapped(mSelectedCell);
						onCellSelected(mSelectedCell);
					}

					if (mAutoHideTouchedCellHint) {
						mTouchedCell = null;
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					mTouchedCell = null;
					break;
			}
			postInvalidate();
		}

		return !mReadonly;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mReadonly) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
					return moveCellSelection(0, -1);
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return moveCellSelection(1, 0);
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return moveCellSelection(0, 1);
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return moveCellSelection(-1, 0);
				case KeyEvent.KEYCODE_0:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_DEL:
					// clear value in selected cell
					// TODO: I'm not really sure that this is thread-safe
					if (mSelectedCell != null) {
						if (event.isShiftPressed() || event.isAltPressed()) {
							setCellNote(mSelectedCell, CellNote.EMPTY);
						} else {
							setCellValue(mSelectedCell, 0);
							moveCellSelectionRight();
						}
					}
					return true;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if (mSelectedCell != null) {
						onCellTapped(mSelectedCell);
					}
					return true;
			}

			if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
				int selNumber = keyCode - KeyEvent.KEYCODE_0;
				Cell cell = mSelectedCell;

				if (event.isShiftPressed() || event.isAltPressed()) {
					// add or remove number in cell's note
					setCellNote(cell, cell.getNote().toggleNumber(selNumber));
				} else {
					// enter number in cell
					setCellValue(cell, selNumber);
					moveCellSelectionRight();
				}
				return true;
			}
		}


		return false;
	}


	/**
	 * Moves selected cell by one cell to the right. If edge is reached, selection
	 * skips on beginning of another line.
	 */
	public void moveCellSelectionRight() {
		if (!moveCellSelection(1, 0)) {
			int selRow = mSelectedCell.getRowIndex();
			selRow++;
			if (!moveCellSelectionTo(selRow, 0)) {
				moveCellSelectionTo(0, 0);
			}
		}
		postInvalidate();
	}

	private void setCellValue(Cell cell, int value) {
		if (cell.isEditable()) {
			if (mGame != null) {
				mGame.setCellValue(cell, value);
			} else {
				cell.setValue(value);
			}
		}
	}

	private void setCellNote(Cell cell, CellNote note) {
		if (cell.isEditable()) {
			if (mGame != null) {
				mGame.setCellNote(cell, note);
			} else {
				cell.setNote(note);
			}
		}
	}


	/**
	 * Moves selected by vx cells right and vy cells down. vx and vy can be negative. Returns true,
	 * if new cell is selected.
	 *
	 * @param vx Horizontal offset, by which move selected cell.
	 * @param vy Vertical offset, by which move selected cell.
	 */
	private boolean moveCellSelection(int vx, int vy) {
		int newRow = 0;
		int newCol = 0;

		if (mSelectedCell != null) {
			newRow = mSelectedCell.getRowIndex() + vy;
			newCol = mSelectedCell.getColumnIndex() + vx;
		}

		return moveCellSelectionTo(newRow, newCol);
	}


	/**
	 * Moves selection to the cell given by row and column index.
	 *
	 * @param row Row index of cell which should be selected.
	 * @param col Columnd index of cell which should be selected.
	 * @return True, if cell was successfuly selected.
	 */
	private boolean moveCellSelectionTo(int row, int col) {
		if (col >= 0 && col < CellCollection.SUDOKU_SIZE
				&& row >= 0 && row < CellCollection.SUDOKU_SIZE) {
			mSelectedCell = mCells.getCell(row, col);
			onCellSelected(mSelectedCell);

			postInvalidate();
			return true;
		}

		return false;
	}

	/**
	 * Returns cell at given screen coordinates. Returns null if no cell is found.
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	private Cell getCellAtPoint(int x, int y) {
		// take into account padding
		int lx = x - getPaddingLeft();
		int ly = y - getPaddingTop();

		int row = (int) (ly / mCellHeight);
		int col = (int) (lx / mCellWidth);

		if (col >= 0 && col < CellCollection.SUDOKU_SIZE
				&& row >= 0 && row < CellCollection.SUDOKU_SIZE) {
			return mCells.getCell(row, col);
		} else {
			return null;
		}
	}

	/**
	 * Occurs when user tap the cell.
	 *
	 * @author romario
	 */
	public interface OnCellTappedListener {
		void onCellTapped(Cell cell);
	}

	/**
	 * Occurs when user selects the cell.
	 *
	 * @author romario
	 */
	public interface OnCellSelectedListener {
		void onCellSelected(Cell cell);
	}

//	private String getMeasureSpecModeString(int mode) {
//		String modeString = null;
//		switch (mode) {
//		case MeasureSpec.AT_MOST:
//			modeString = "MeasureSpec.AT_MOST";
//			break;
//		case MeasureSpec.EXACTLY:
//			modeString = "MeasureSpec.EXACTLY";
//			break;
//		case MeasureSpec.UNSPECIFIED:
//			modeString = "MeasureSpec.UNSPECIFIED";
//			break;
//		}
//		
//		if (modeString == null)
//			modeString = new Integer(mode).toString();
//		
//		return modeString;
//	}

}
