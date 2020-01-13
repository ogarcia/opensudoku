package org.moire.opensudoku.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.moire.opensudoku.R;

import java.util.List;

public class AndroidUtils {
    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     *
     * @param context The application's environment.
     * @param action  The Intent action to check for availability.
     * @return True if an Intent with the specified action can be sent and
     * responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static int getThemeResourceIdFromString(String theme){
        switch (theme) {
            case "default":
                return R.style.Theme_Default;
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

    public static void setThemeFromPreferences(Context context) {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = gameSettings.getString("theme", "default");
        context.setTheme(getThemeResourceIdFromString(theme));
    }

    /**
     * Returns version code of OpenSudoku.
     *
     * @return
     */
    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns version name of OpenSudoku.
     *
     * @return
     */
    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
