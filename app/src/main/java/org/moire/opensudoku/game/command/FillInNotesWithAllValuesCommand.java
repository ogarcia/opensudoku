package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.CellCollection;

public class FillInNotesWithAllValuesCommand extends AbstractMultiNoteCommand {

    public FillInNotesWithAllValuesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldCornerNotes.clear();
        saveOldNotes();

        cells.fillInCornerNotesWithAllValues();
    }
}
