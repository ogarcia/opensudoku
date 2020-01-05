package org.moire.opensudoku.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.gui.SudokuBoardView;

public class ThemeUtils {

    public static void applyThemeToSudokuBoardViewFromContext(String theme, SudokuBoardView board, Context context) {
        if (theme.equals("custom")) {

            SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
            board.setLineColor(gameSettings.getInt("custom_theme_lineColor", R.color.default_lineColor));
            board.setSectorLineColor(gameSettings.getInt("custom_theme_sectorLineColor", R.color.default_sectorLineColor));
            board.setTextColor(gameSettings.getInt("custom_theme_textColor", R.color.default_textColor));
            board.setTextColorReadOnly(gameSettings.getInt("custom_theme_textColorReadOnly", R.color.default_textColorReadOnly));
            board.setTextColorNote(gameSettings.getInt("custom_theme_textColorNote", R.color.default_textColorNote));
            board.setBackgroundColor(gameSettings.getInt("custom_theme_backgroundColor", R.color.default_backgroundColor));
            board.setBackgroundColorSecondary(gameSettings.getInt("custom_theme_backgroundColorSecondary", R.color.default_backgroundColorSecondary));
            board.setBackgroundColorReadOnly(gameSettings.getInt("custom_theme_backgroundColorReadOnly", R.color.default_backgroundColorReadOnly));
            board.setBackgroundColorTouched(gameSettings.getInt("custom_theme_backgroundColorTouched", R.color.default_backgroundColorTouched));
            board.setBackgroundColorSelected(gameSettings.getInt("custom_theme_backgroundColorSelected", R.color.default_backgroundColorSelected));
            board.setBackgroundColorHighlighted(gameSettings.getInt("custom_theme_backgroundColorHighlighted", R.color.default_backgroundColorHighlighted));
        } else {
            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, AndroidUtils.getThemeResourceIdFromString(theme));

            int[] attributes = {
                    R.attr.lineColor,
                    R.attr.sectorLineColor,
                    R.attr.textColor,
                    R.attr.textColorReadOnly,
                    R.attr.textColorNote,
                    R.attr.backgroundColor,
                    R.attr.backgroundColorSecondary,
                    R.attr.backgroundColorReadOnly,
                    R.attr.backgroundColorTouched,
                    R.attr.backgroundColorSelected,
                    R.attr.backgroundColorHighlighted
            };

            TypedArray themeColors = themeWrapper.getTheme().obtainStyledAttributes(attributes);
            board.setLineColor(themeColors.getColor(0, R.color.default_lineColor));
            board.setSectorLineColor(themeColors.getColor(1, R.color.default_sectorLineColor));
            board.setTextColor(themeColors.getColor(2, R.color.default_textColor));
            board.setTextColorReadOnly(themeColors.getColor(3, R.color.default_textColorReadOnly));
            board.setTextColorNote(themeColors.getColor(4, R.color.default_textColorNote));
            board.setBackgroundColor(themeColors.getColor(5, R.color.default_backgroundColor));
            board.setBackgroundColorSecondary(themeColors.getColor(6, R.color.default_backgroundColorSecondary));
            board.setBackgroundColorReadOnly(themeColors.getColor(7, R.color.default_backgroundColorReadOnly));
            board.setBackgroundColorTouched(themeColors.getColor(8, R.color.default_backgroundColorTouched));
            board.setBackgroundColorSelected(themeColors.getColor(9, R.color.default_backgroundColorSelected));
            board.setBackgroundColorHighlighted(themeColors.getColor(10, R.color.default_backgroundColorHighlighted));
        }
        board.invalidate();
    }

    public static void prepareSudokuPreviewView(SudokuBoardView board) {
        board.setFocusable(false);

        // Create a sample game by starting with the debug game, removing an extra box (sector),
        // adding in notes, nd filling in the first 3 clues. This provides a sample of an
        // in-progress game that will demonstrate all of the possible scenarios that have different
        // theme colors applied to them.
        CellCollection cells = CellCollection.createDebugGame();
        cells.getCell(0, 3).setValue(0);
        cells.getCell(0, 4).setValue(0);
        cells.getCell(1, 3).setValue(0);
        cells.getCell(1, 5).setValue(0);
        cells.getCell(2, 4).setValue(0);
        cells.getCell(2, 5).setValue(0);

        cells.getCell(0, 0).setValue(1);
        cells.getCell(0, 1).setValue(2);
        cells.getCell(0, 2).setValue(3);
        cells.fillInNotes();
        board.setCells(cells);
    }
}
