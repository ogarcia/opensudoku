package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.CellCollection;

public class FillInNotesCommand extends AbstractMultiNoteCommand {

    public FillInNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldCornerNotes.clear();
        saveOldNotes();

        cells.fillInCornerNotes();
    }
}
