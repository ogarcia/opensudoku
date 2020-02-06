package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.Cell;

import java.util.StringTokenizer;

public class SetCellValueAndRemoveNotesCommand extends AbstractMultiNoteCommand {

    private int mCellRow;
    private int mCellColumn;
    private int mValue;
    private int mOldValue;

    private Cell getCell() {
        return getCells().getCell(mCellRow, mCellColumn);
    }

    public SetCellValueAndRemoveNotesCommand(Cell cell, int value) {
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
        mValue = value;
    }

    SetCellValueAndRemoveNotesCommand() {
    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mCellRow).append("|");
        data.append(mCellColumn).append("|");
        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
        mCellRow = Integer.parseInt(data.nextToken());
        mCellColumn = Integer.parseInt(data.nextToken());
    }

    @Override
    void execute() {
        mOldNotes.clear();
        saveOldNotes();

        Cell cell = getCell();
        getCells().removeNotesForChangedCell(cell, mValue);
        mOldValue = cell.getValue();
        cell.setValue(mValue);
    }

    @Override
    void undo() {
        super.undo();
        Cell cell = getCell();
        cell.setValue(mOldValue);
    }
}
