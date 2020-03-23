package org.moire.opensudoku.gui;

import android.content.Context;

import org.moire.opensudoku.db.SudokuColumns;

public class SudokuListSorter {

    public static final int SORT_BY_CREATED = 0;
    public static final int SORT_BY_TIME = 1;
    public static final int SORT_BY_LAST_PLAYED = 2;

    private static final int SORT_TYPE_OPTIONS_LENGTH = 3;

    private Context context;
    private int sortType;

    public SudokuListSorter(Context context) {
        this(context, SORT_BY_CREATED);
    }

    public SudokuListSorter(Context context, int sortType) {
        this.context = context;
        this.sortType =
                sortType >= 0 && sortType < SORT_TYPE_OPTIONS_LENGTH ? sortType : SORT_BY_CREATED;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType =
                sortType >= 0 && sortType < SORT_TYPE_OPTIONS_LENGTH ? sortType : SORT_BY_CREATED;
    }

    public String getSortOrder() {
        switch (sortType) {
            case SORT_BY_CREATED:
                return SudokuColumns.CREATED + " DESC";
            case SORT_BY_TIME:
                return SudokuColumns.TIME + " DESC";
            case SORT_BY_LAST_PLAYED:
                return SudokuColumns.LAST_PLAYED + " DESC";
        }

        return SudokuColumns.CREATED + " DESC";
    }

}
