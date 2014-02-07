package org.moire.opensudoku.game.command;

import java.util.Stack;

import org.moire.opensudoku.game.CellCollection;

import android.os.Bundle;

public class CommandStack {
	private Stack<AbstractCommand> mCommandStack = new Stack<AbstractCommand>();

	// TODO: I need cells collection, because I have to call validate on it after some
	//	commands. CellCollection should be able to validate itself on change.
	private CellCollection mCells;

	public CommandStack(CellCollection cells) {
		mCells = cells;
	}

	public void saveState(Bundle outState) {
		outState.putInt("cmdStack.size", mCommandStack.size());
		for (int i = 0; i < mCommandStack.size(); i++) {
			AbstractCommand command = mCommandStack.get(i);
			Bundle commandState = new Bundle();
			commandState.putString("commandClass", command.getCommandClass());
			command.saveState(commandState);
			outState.putBundle("cmdStack." + i, commandState);
		}
	}

	public void restoreState(Bundle inState) {
		int stackSize = inState.getInt("cmdStack.size");
		for (int i = 0; i < stackSize; i++) {
			Bundle commandState = inState.getBundle("cmdStack." + i);
			AbstractCommand command = AbstractCommand.newInstance(commandState.getString("commandClass"));
			command.restoreState(commandState);
			push(command);
		}
	}

	public boolean empty() {
		return mCommandStack.empty();
	}

	public void execute(AbstractCommand command) {
		push(command);
		command.execute();
	}

	public void undo() {
		if (!mCommandStack.empty()) {
			AbstractCommand c = pop();
			c.undo();
			validateCells();
		}
	}

	public void setCheckpoint() {
		if (!mCommandStack.empty()) {
			AbstractCommand c = mCommandStack.peek();
			c.setCheckpoint(true);
		}
	}

	public boolean hasCheckpoint() {
		for (AbstractCommand c : mCommandStack) {
			if (c.isCheckpoint())
				return true;
		}
		return false;
	}

	public void undoToCheckpoint() {
		/*
		 * I originally planned to just call undo but this way it doesn't need to 
		 * validateCells() until the run is complete
		 */
		AbstractCommand c;
		while (!mCommandStack.empty()) {
			c = mCommandStack.pop();
			c.undo();

			if (mCommandStack.empty() || mCommandStack.peek().isCheckpoint()) {
				break;
			}
		}
		validateCells();
	}


	public boolean hasSomethingToUndo() {
		return mCommandStack.size() != 0;
	}

	private void push(AbstractCommand command) {
		if (command instanceof AbstractCellCommand) {
			((AbstractCellCommand) command).setCells(mCells);
		}
		mCommandStack.push(command);
	}

	private AbstractCommand pop() {
		return mCommandStack.pop();
	}

	private void validateCells() {
		mCells.validate();
	}


}
