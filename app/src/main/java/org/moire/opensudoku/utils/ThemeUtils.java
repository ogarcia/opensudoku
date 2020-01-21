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
                return R.style.Theme_Default;
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
            themeResource.applyStyle(getPrimaryColorResourceId(context, gameSettings.getInt("custom_theme_backgroundColor", 0)), true);
            themeResource.applyStyle(getDarkPrimaryColorResourceId(context, gameSettings.getInt("custom_theme_backgroundColorReadOnly", 0)), true);
            themeResource.applyStyle(getAccentColorResourceId(context, gameSettings.getInt("custom_theme_backgroundColorTouched", 0)), true);
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

    private static final int[] MATERIAL_COLORS = {
            0xfde0dc,
            0xf9bdbb,
            0xf69988,
            0xf36c60,
            0xe84e40,
            0xe51c23,
            0xdd191d,
            0xd01716,
            0xc41411,
            0xb0120a,
            0xff7997,
            0xff5177,
            0xff2d6f,
            0xe00032,
            0xfce4ec,
            0xf8bbd0,
            0xf48fb1,
            0xf06292,
            0xec407a,
            0xe91e63,
            0xd81b60,
            0xc2185b,
            0xad1457,
            0x880e4f,
            0xff80ab,
            0xff4081,
            0xf50057,
            0xc51162,
            0xf3e5f5,
            0xe1bee7,
            0xce93d8,
            0xba68c8,
            0xab47bc,
            0x9c27b0,
            0x8e24aa,
            0x7b1fa2,
            0x6a1b9a,
            0x4a148c,
            0xea80fc,
            0xe040fb,
            0xd500f9,
            0xaa00ff,
            0xede7f6,
            0xd1c4e9,
            0xb39ddb,
            0x9575cd,
            0x7e57c2,
            0x673ab7,
            0x5e35b1,
            0x512da8,
            0x4527a0,
            0x311b92,
            0xb388ff,
            0x7c4dff,
            0x651fff,
            0x6200ea,
            0xe8eaf6,
            0xc5cae9,
            0x9fa8da,
            0x7986cb,
            0x5c6bc0,
            0x3f51b5,
            0x3949ab,
            0x303f9f,
            0x283593,
            0x1a237e,
            0x8c9eff,
            0x536dfe,
            0x3d5afe,
            0x304ffe,
            0xe7e9fd,
            0xd0d9ff,
            0xafbfff,
            0x91a7ff,
            0x738ffe,
            0x5677fc,
            0x4e6cef,
            0x455ede,
            0x3b50ce,
            0x2a36b1,
            0xa6baff,
            0x6889ff,
            0x4d73ff,
            0x4d69ff,
            0xe1f5fe,
            0xb3e5fc,
            0x81d4fa,
            0x4fc3f7,
            0x29b6f6,
            0x03a9f4,
            0x039be5,
            0x0288d1,
            0x0277bd,
            0x01579b,
            0x80d8ff,
            0x40c4ff,
            0x00b0ff,
            0x0091ea,
            0xe0f7fa,
            0xb2ebf2,
            0x80deea,
            0x4dd0e1,
            0x26c6da,
            0x00bcd4,
            0x00acc1,
            0x0097a7,
            0x00838f,
            0x006064,
            0x84ffff,
            0x18ffff,
            0x00e5ff,
            0x00b8d4,
            0xe0f2f1,
            0xb2dfdb,
            0x80cbc4,
            0x4db6ac,
            0x26a69a,
            0x009688,
            0x00897b,
            0x00796b,
            0x00695c,
            0x004d40,
            0xa7ffeb,
            0x64ffda,
            0x1de9b6,
            0x00bfa5,
            0xd0f8ce,
            0xa3e9a4,
            0x72d572,
            0x42bd41,
            0x2baf2b,
            0x259b24,
            0x0a8f08,
            0x0a7e07,
            0x056f00,
            0x0d5302,
            0xa2f78d,
            0x5af158,
            0x14e715,
            0x12c700,
            0xf1f8e9,
            0xdcedc8,
            0xc5e1a5,
            0xaed581,
            0x9ccc65,
            0x8bc34a,
            0x7cb342,
            0x689f38,
            0x558b2f,
            0x33691e,
            0xccff90,
            0xb2ff59,
            0x76ff03,
            0x64dd17,
            0xf9fbe7,
            0xf0f4c3,
            0xe6ee9c,
            0xdce775,
            0xd4e157,
            0xcddc39,
            0xc0ca33,
            0xafb42b,
            0x9e9d24,
            0x827717,
            0xf4ff81,
            0xeeff41,
            0xc6ff00,
            0xaeea00,
            0xfffde7,
            0xfff9c4,
            0xfff59d,
            0xfff176,
            0xffee58,
            0xffeb3b,
            0xfdd835,
            0xfbc02d,
            0xf9a825,
            0xf57f17,
            0xffff8d,
            0xffff00,
            0xffea00,
            0xffd600,
            0xfff8e1,
            0xffecb3,
            0xffe082,
            0xffd54f,
            0xffca28,
            0xffc107,
            0xffb300,
            0xffa000,
            0xff8f00,
            0xff6f00,
            0xffe57f,
            0xffd740,
            0xffc400,
            0xffab00,
            0xfff3e0,
            0xffe0b2,
            0xffcc80,
            0xffb74d,
            0xffa726,
            0xff9800,
            0xfb8c00,
            0xf57c00,
            0xef6c00,
            0xe65100,
            0xffd180,
            0xffab40,
            0xff9100,
            0xff6d00,
            0xfbe9e7,
            0xffccbc,
            0xffab91,
            0xff8a65,
            0xff7043,
            0xff5722,
            0xf4511e,
            0xe64a19,
            0xd84315,
            0xbf360c,
            0xff9e80,
            0xff6e40,
            0xff3d00,
            0xdd2c00,
            0xefebe9,
            0xd7ccc8,
            0xbcaaa4,
            0xa1887f,
            0x8d6e63,
            0x795548,
            0x6d4c41,
            0x5d4037,
            0x4e342e,
            0x3e2723,
            0xfafafa,
            0xf5f5f5,
            0xeeeeee,
            0xe0e0e0,
            0xbdbdbd,
            0x9e9e9e,
            0x757575,
            0x616161,
            0x424242,
            0x212121,
            0x000000,
            0xffffff,
            0xeceff1,
            0xcfd8dc,
            0xb0bec5,
            0x90a4ae,
            0x78909c,
            0x607d8b,
            0x546e7a,
            0x455a64,
            0x37474f,
            0x263238
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
        String colorAsString = String.format("0x%1$06x", findClosestMaterialColor(color));
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
}
