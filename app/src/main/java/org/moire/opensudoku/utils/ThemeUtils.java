package org.moire.opensudoku.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import java.util.Map;

public class ThemeUtils {

    public static int getThemeResourceIdFromString(String theme){
        switch (theme) {
            case "default":
                return R.style.Theme_Default;
            case "opensudoku":
                return R.style.Theme_OpenSudoku;
            case "amoled":
                return R.style.Theme_AMOLED;
            case "latte":
                return R.style.Theme_Latte;
            case "espresso":
                return R.style.Theme_Espresso;
            case "sunrise":
                return R.style.Theme_Sunrise;
            case "honeybee":
                return R.style.Theme_HoneyBee;
            case "crystal":
                return R.style.Theme_Crystal;
            case "midnight_blue":
                return R.style.Theme_MidnightBlue;
            case "emerald":
                return R.style.Theme_Emerald;
            case "forest":
                return R.style.Theme_Forest;
            case "amethyst":
                return R.style.Theme_Amethyst;
            case "ruby":
                return R.style.Theme_Ruby;
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
            case "custom":
                return R.style.Theme_AppCompat;
            case "custom_light":
                return R.style.Theme_AppCompat_Light;
            default:
                return R.style.Theme_OpenSudoku;
        }
    }

    public static boolean isLightTheme(String theme){
        switch (theme) {
            case "default":
            case "amoled":
            case "espresso":
            case "honeybee":
            case "midnight_blue":
            case "forest":
            case "ruby":
            case "paper":
            case "graphpaper":
            case "highcontrast":
            case "custom":
                return false;
            case "opensudoku":
            case "latte":
            case "sunrise":
            case "crystal":
            case "emerald":
            case "amethyst":
            case "light":
            case "paperlight":
            case "graphpaperlight":
            case "invertedhighcontrast":
            case "custom_light":
            default:
                return true;
        }
    }

    public static String getCurrentThemeFromPreferences(Context context) {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        return gameSettings.getString("theme", "default");
    }

    public static int getThemeResourceIdFromPreferences(Context context) {
        return getThemeResourceIdFromString(getCurrentThemeFromPreferences(context));
    }

    public static void setThemeFromPreferences(Context context) {
        String theme = getCurrentThemeFromPreferences(context);
        context.setTheme(getThemeResourceIdFromString(theme));

        if (theme.equals("custom") || theme.equals("custom_light")) {
            Resources.Theme themeResource = context.getTheme();
            SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
            themeResource.applyStyle(getPrimaryColorResourceId(context, gameSettings.getInt("custom_theme_colorPrimary", 0xff49B7AC)), true);
            themeResource.applyStyle(getDarkPrimaryColorResourceId(context, gameSettings.getInt("custom_theme_colorPrimaryDark", 0xff009587)), true);
            themeResource.applyStyle(getAccentColorResourceId(context, gameSettings.getInt("custom_theme_colorAccent", 0xff656565)), true);
        }
    }

    public static int getCurrentThemeColor(Context context, int colorAttribute) {
        int[] attributes = {colorAttribute};
        TypedArray themeColors = context.getTheme().obtainStyledAttributes(attributes);
        return themeColors.getColor(0, Color.BLACK);
    }

    public static int getCurrentThemeStyle(Context context, int styleAttribute) {
        int[] attributes = {styleAttribute};
        TypedArray themeStyles = context.getTheme().obtainStyledAttributes(attributes);
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
        if (theme.equals("custom") || theme.equals("custom_light")) {

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
        board.in
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

    private static final int[] MATERIAL_COLORS = {
            0xfffde0dc,
            0xfff9bdbb,
            0xfff69988,
            0xfff36c60,
            0xffe84e40,
            0xffe51c23,
            0xffdd191d,
            0xffd01716,
            0xffc41411,
            0xffb0120a,
            0xffff7997,
            0xffff5177,
            0xffff2d6f,
            0xffe00032,
            0xfffce4ec,
            0xfff8bbd0,
            0xfff48fb1,
            0xfff06292,
            0xffec407a,
            0xffe91e63,
            0xffd81b60,
            0xffc2185b,
            0xffad1457,
            0xff880e4f,
            0xffff80ab,
            0xffff4081,
            0xfff50057,
            0xffc51162,
            0xfff3e5f5,
            0xffe1bee7,
            0xffce93d8,
            0xffba68c8,
            0xffab47bc,
            0xff9c27b0,
            0xff8e24aa,
            0xff7b1fa2,
            0xff6a1b9a,
            0xff4a148c,
            0xffea80fc,
            0xffe040fb,
            0xffd500f9,
            0xffaa00ff,
            0xffede7f6,
            0xffd1c4e9,
            0xffb39ddb,
            0xff9575cd,
            0xff7e57c2,
            0xff673ab7,
            0xff5e35b1,
            0xff512da8,
            0xff4527a0,
            0xff311b92,
            0xffb388ff,
            0xff7c4dff,
            0xff651fff,
            0xff6200ea,
            0xffe8eaf6,
            0xffc5cae9,
            0xff9fa8da,
            0xff7986cb,
            0xff5c6bc0,
            0xff3f51b5,
            0xff3949ab,
            0xff303f9f,
            0xff283593,
            0xff1a237e,
            0xff8c9eff,
            0xff536dfe,
            0xff3d5afe,
            0xff304ffe,
            0xffe7e9fd,
            0xffd0d9ff,
            0xffafbfff,
            0xff91a7ff,
            0xff738ffe,
            0xff5677fc,
            0xff4e6cef,
            0xff455ede,
            0xff3b50ce,
            0xff2a36b1,
            0xffa6baff,
            0xff6889ff,
            0xff4d73ff,
            0xff4d69ff,
            0xffe1f5fe,
            0xffb3e5fc,
            0xff81d4fa,
            0xff4fc3f7,
            0xff29b6f6,
            0xff03a9f4,
            0xff039be5,
            0xff0288d1,
            0xff0277bd,
            0xff01579b,
            0xff80d8ff,
            0xff40c4ff,
            0xff00b0ff,
            0xff0091ea,
            0xffe0f7fa,
            0xffb2ebf2,
            0xff80deea,
            0xff4dd0e1,
            0xff26c6da,
            0xff00bcd4,
            0xff00acc1,
            0xff0097a7,
            0xff00838f,
            0xff006064,
            0xff84ffff,
            0xff18ffff,
            0xff00e5ff,
            0xff00b8d4,
            0xffe0f2f1,
            0xffb2dfdb,
            0xff80cbc4,
            0xff4db6ac,
            0xff26a69a,
            0xff009688,
            0xff00897b,
            0xff00796b,
            0xff00695c,
            0xff004d40,
            0xffa7ffeb,
            0xff64ffda,
            0xff1de9b6,
            0xff00bfa5,
            0xffd0f8ce,
            0xffa3e9a4,
            0xff72d572,
            0xff42bd41,
            0xff2baf2b,
            0xff259b24,
            0xff0a8f08,
            0xff0a7e07,
            0xff056f00,
            0xff0d5302,
            0xffa2f78d,
            0xff5af158,
            0xff14e715,
            0xff12c700,
            0xfff1f8e9,
            0xffdcedc8,
            0xffc5e1a5,
            0xffaed581,
            0xff9ccc65,
            0xff8bc34a,
            0xff7cb342,
            0xff689f38,
            0xff558b2f,
            0xff33691e,
            0xffccff90,
            0xffb2ff59,
            0xff76ff03,
            0xff64dd17,
            0xfff9fbe7,
            0xfff0f4c3,
            0xffe6ee9c,
            0xffdce775,
            0xffd4e157,
            0xffcddc39,
            0xffc0ca33,
            0xffafb42b,
            0xff9e9d24,
            0xff827717,
            0xfff4ff81,
            0xffeeff41,
            0xffc6ff00,
            0xffaeea00,
            0xfffffde7,
            0xfffff9c4,
            0xfffff59d,
            0xfffff176,
            0xffffee58,
            0xffffeb3b,
            0xfffdd835,
            0xfffbc02d,
            0xfff9a825,
            0xfff57f17,
            0xffffff8d,
            0xffffff00,
            0xffffea00,
            0xffffd600,
            0xfffff8e1,
            0xffffecb3,
            0xffffe082,
            0xffffd54f,
            0xffffca28,
            0xffffc107,
            0xffffb300,
            0xffffa000,
            0xffff8f00,
            0xffff6f00,
            0xffffe57f,
            0xffffd740,
            0xffffc400,
            0xffffab00,
            0xfffff3e0,
            0xffffe0b2,
            0xffffcc80,
            0xffffb74d,
            0xffffa726,
            0xffff9800,
            0xfffb8c00,
            0xfff57c00,
            0xffef6c00,
            0xffe65100,
            0xffffd180,
            0xffffab40,
            0xffff9100,
            0xffff6d00,
            0xfffbe9e7,
            0xffffccbc,
            0xffffab91,
            0xffff8a65,
            0xffff7043,
            0xffff5722,
            0xfff4511e,
            0xffe64a19,
            0xffd84315,
            0xffbf360c,
            0xffff9e80,
            0xffff6e40,
            0xffff3d00,
            0xffdd2c00,
            0xffefebe9,
            0xffd7ccc8,
            0xffbcaaa4,
            0xffa1887f,
            0xff8d6e63,
            0xff795548,
            0xff6d4c41,
            0xff5d4037,
            0xff4e342e,
            0xff3e2723,
            0xfffafafa,
            0xfff5f5f5,
            0xffeeeeee,
            0xffe0e0e0,
            0xffbdbdbd,
            0xff9e9e9e,
            0xff757575,
            0xff616161,
            0xff424242,
            0xff212121,
            0xff000000,
            0xffffffff,
            0xffeceff1,
            0xffcfd8dc,
            0xffb0bec5,
            0xff90a4ae,
            0xff78909c,
            0xff607d8b,
            0xff546e7a,
            0xff455a64,
            0xff37474f,
            0xff263238
    };

    public static int findClosestMaterialColor(int color) {
        int minDifference = Integer.MAX_VALUE;
        int selectedIndex = 0;
        int difference = 0;
        int rdiff = 0;
        int gdiff = 0;
        int bdiff = 0;

        for (int i = 0; i < MATERIAL_COLORS.length; i++) {
            if (color == MATERIAL_COLORS[i]) {
                return color;
            }

            rdiff = Math.abs(Color.red(color) - Color.red(MATERIAL_COLORS[i]));
            gdiff = Math.abs(Color.green(color) - Color.green(MATERIAL_COLORS[i]));
            bdiff = Math.abs(Color.blue(color) - Color.blue(MATERIAL_COLORS[i]));
            difference = rdiff + gdiff + bdiff;
            if (difference < minDifference) {
                minDifference = difference;
                selectedIndex = i;
            }
        }

        return MATERIAL_COLORS[selectedIndex];
    }

    private static int getColorResourceIdHelper(Context context, String style, int color) {
        String colorAsString = String.format("%1$06x", (color & 0x00FFFFFF));
        return context.getResources().getIdentifier(style + colorAsString, "style", context.getPackageName());
    }

    public static int getPrimaryColorResourceId(Context context, int color) {
        return getColorResourceIdHelper(context, "colorPrimary_", color);
    }

    public static int getDarkPrimaryColorResourceId(Context context, int color) {
        return getColorResourceIdHelper(context, "colorPrimaryDark_", color);
    }

    public static int getAccentColorResourceId(Context context, int color) {
        return getColorResourceIdHelper(context, "colorAccent_", color);
    }

    public static long sTimestampOfLastThemeUpdate = 0;
}
