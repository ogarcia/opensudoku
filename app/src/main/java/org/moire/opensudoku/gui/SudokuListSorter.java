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
    private boolean ascending;

    public SudokuListSorter(Context context) {
        this(context, SORT_BY_CREATED, false);
    }

    public SudokuListSorter(Context context, int sortType, boolean ascending) {
        this.context = context;
        this.sortType =
                sortType >= 0 && sortType < SORT_TYPE_OPTIONS_LENGTH ? sortType : SORT_BY_CREATED;
        this.ascending = ascending;
    }

    public int getSortType() {
        return sortType;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setSortType(int sortType) {
        this.sortType =
                sortType >= 0 && sortType < SORT_TYPE_OPTIONS_LENGTH ? sortType : SORT_BY_CREATED;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getSortOrder() {
        String order = ascending ? " ASC" : " DESC";
        switch (sortType) {
            case SORT_BY_CREATED:
                return SudokuColumns.CREATED + order;
            case SORT_BY_TIME:
                return SudokuColumns.TIME + order;
            case SORT_BY_LAST_PLAYED:
                return SudokuColumns.LAST_PLAYED + order;
        }

        return SudokuColumns.CREATED + order;
    }

}
