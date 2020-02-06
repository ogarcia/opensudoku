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

package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.Cell;

import java.util.StringTokenizer;

public class SetCellValueCommand extends AbstractSingleCellCommand {

    private int mValue;
    private int mOldValue;

    public SetCellValueCommand(Cell cell, int value) {
        super(cell);
        mValue = value;
    }

    SetCellValueCommand() {
    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
    }

    @Override
    void execute() {
        Cell cell = getCell();
        mOldValue = cell.getValue();
        cell.setValue(mValue);
    }

    @Override
    void undo() {
        Cell cell = getCell();
        cell.setValue(mOldValue);
    }

}
