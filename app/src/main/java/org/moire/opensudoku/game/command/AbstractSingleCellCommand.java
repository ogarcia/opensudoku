package org.moire.opensudoku.game.command;

import android.os.Bundle;

import org.moire.opensudoku.game.Cell;

/**
 * Created by spimanov on 30.10.17.
 */

public abstract class AbstractSingleCellCommand extends AbstractCellCommand {

    private int mCellRow;
    private int mCellColumn;

    public AbstractSingleCellCommand(Cell cell){
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
    }

    AbstractSingleCellCommand() {

    }

    @Override
    void saveState(Bundle outState) {
        super.saveState(outState);

        outState.putInt("cellRow", mCellRow);
        outState.putInt("cellColumn", mCellColumn);
    }

    @Override
    void restoreState(Bundle inState) {
        super.restoreState(inState);

        mCellRow = inState.getInt("cellRow");
        mCellColumn = inState.getInt("cellColumn");
    }

    Cell getCell() {
        return getCells().getCell(mCellRow, mCellColumn);
    }

    public int getCellRow() {
        return mCellRow;
    }

    public int getCellColumn() {
        return mCellColumn;
    }
}
