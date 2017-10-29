package org.moire.opensudoku.game.command;

import android.os.Bundle;

/**
 * Created by spimanov on 29.10.17.
 */

public class CheckpointCommand extends AbstractCommand {

    public CheckpointCommand() {
    }

    @Override
    void saveState(Bundle outState) {
        super.saveState(outState);
    }

    @Override
    void restoreState(Bundle inState) {
        super.restoreState(inState);
    }

    @Override
    void execute() {
    }

    @Override
    void undo() {
    }

}
