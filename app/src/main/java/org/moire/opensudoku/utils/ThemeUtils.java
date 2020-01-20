package org.moire.opensudoku.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.gui.SudokuBoardView;

public class ThemeUtils {

    public static int getThemeResourceIdFromString(String theme){
        switch (theme) {
            case "default":
                return R.style.Theme_Default;
            case "opensudoku":
                return R.style.Theme_OpenSudoku;
            case "paper":
                return R.style.Theme_Paper;
            case "graphpaper":
                return R.style.Theme_GraphPaper;
            case "light":
                return R.style.Theme_Light;
            case "paperlight":
                return R.style.Theme_PaperLight;
            case "graphpaperlight":
                return R.style.Theme_GraphPaperLight;
            case "highcontrast":
                return R.style.Theme_HighContrast;
            case "invertedhighcontrast":
                return R.style.Theme_InvertedHighContrast;
            default:
                return R.style.Theme_Default;
        }
    }

    public static int getThemeResourceIdFromPreferences(Context context) {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = gameSettings.getString("theme", "default");
        return getThemeResourceIdFromString(theme);
    }

    public static void setThemeFromPreferences(Context context) {
        context.setTheme(getThemeResourceIdFromPreferences(context));
    }

    public static int getCurrentThemeColor(Context context, int colorAttribute) {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = gameSettings.getString("theme", "default");
        return getThemeColor(theme, context, colorAttribute);
    }

    public static int getThemeColor(String theme, Context context, int colorAttribute) {

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, getThemeResourceIdFromString(theme));
        int[] attributes = {colorAttribute};
        TypedArray themeColors = themeWrapper.getTheme().obtainStyledAttributes(attributes);
        return themeColors.getColor(0, Color.BLACK);
    }

    public static int getCurrentThemeStyle(Context context, int styleAttribute) {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = gameSettings.getString("theme", "default");
        return getThemeStyle(theme, context, styleAttribute);
    }

    public static int getThemeStyle(String theme, Context context, int styleAttribute) {

        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, getThemeResourceIdFromString(theme));
        int[] attributes = {styleAttribute};
        TypedArray themeStyles = themeWrapper.getTheme().obtainStyledAttributes(attributes);
        return themeStyles.getResourceId(0, 0);
    }

    public enum IMButtonStyle {
        DEFAULT,            // no background tint, default text color
        ACCENT,             // accent background tint, inverse text color
        ACCENT_HIGHCONTRAST // inverse text color background, default text color
    }

    public static void applyIMButtonStateToView(TextView view, IMButtonStyle style) {
        switch (style) {
            case DEFAULT:
                view.getBackground().setColorFilter(null);
                view.setTextColor(getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimary));
                break;

            case ACCENT:
                view.getBackground().setColorFilter(getCurrentThemeColor(view.getContext(), android.R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
                view.setTextColor(getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimaryInverse));
                break;

            case ACCENT_HIGHCONTRAST:
                view.getBackground().setColorFilter(getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimaryInverse), PorterDuff.Mode.SRC_ATOP);
                view.setTextColor(getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimary));
                break;
        }
    }

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
            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, getThemeResourceIdFromString(theme));

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
        // adding in notes, and filling in the first 3 clues. This provides a sample of an
        // in-progress game that will demonstrate all of the possible scenarios that have different
        // theme colors applied to them.
        CellCollection cells = CellCollection.createDebugGame();
        cells.getCell(0, 3).setValue(0);
        cells.getCell(0, 4).setValue(0);
        cells.getCell(1, 3).setValue(0);
        cells.getCell(1, 5).setValue(0);
        cells.getCell(2, 4).setValue(0);
        cells.getCell(2, 5).setValue(0);
        cells.markAllCellsAsEditable();
        cells.markFilledCellsAsNotEditable();

        cells.getCell(0, 0).setValue(1);
        cells.getCell(0, 1).setValue(2);
        cells.getCell(0, 2).setValue(3);

        cells.fillInNotes();
        board.setCells(cells);
    }
}
