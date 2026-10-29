package org.levimc.pojavcontrols;

import android.content.Context;
import android.content.SharedPreferences;

/** Pengaturan global kontrol: sensitivitas kamera dan perilaku saat menu terbuka. */
final class ControlSettings {
    static final int SENS_MIN = 25;
    static final int SENS_MAX = 400;
    static final int SENS_DEFAULT = 100;
    static final int CURSOR_MIN = 50;
    static final int CURSOR_MAX = 500;
    static final int CURSOR_DEFAULT = 200;

    private static final String PREFS = "pojav_controls_settings";
    private static final String KEY_SENS = "look_sensitivity_percent";
    private static final String KEY_HIDE_IN_MENU = "hide_controls_in_menu";
    private static final String KEY_CURSOR = "cursor_speed_percent";

    private static volatile int sensitivityPercent = -1;
    private static volatile int cursorSpeedPercent = -1;
    private static volatile int hideInMenu = -1;  // -1 = belum dibaca, 0 = tidak, 1 = ya

    private ControlSettings() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static int clampSensitivity(int percent) {
        return Math.max(SENS_MIN, Math.min(SENS_MAX, percent));
    }

    static int sensitivityPercent(Context context) {
        int cached = sensitivityPercent;
        if (cached >= 0) return cached;
        int loaded = clampSensitivity(prefs(context).getInt(KEY_SENS, SENS_DEFAULT));
        sensitivityPercent = loaded;
        return loaded;
    }

    static float sensitivityFactor(Context context) {
        return sensitivityPercent(context) / 100f;
    }

    static void setSensitivityPercent(Context context, int percent) {
        int value = clampSensitivity(percent);
        sensitivityPercent = value;
        prefs(context).edit().putInt(KEY_SENS, value).apply();
    }

    static int cursorSpeedPercent(Context context) {
        int cached = cursorSpeedPercent;
        if (cached >= 0) return cached;
        int loaded = Math.max(CURSOR_MIN, Math.min(CURSOR_MAX,
                prefs(context).getInt(KEY_CURSOR, CURSOR_DEFAULT)));
        cursorSpeedPercent = loaded;
        return loaded;
    }

    static float cursorFactor(Context context) {
        return cursorSpeedPercent(context) / 100f;
    }

    static void setCursorSpeedPercent(Context context, int percent) {
        int value = Math.max(CURSOR_MIN, Math.min(CURSOR_MAX, percent));
        cursorSpeedPercent = value;
        prefs(context).edit().putInt(KEY_CURSOR, value).apply();
    }

    /** Kalau aktif, semua tombol disembunyikan dan sentuhan diteruskan langsung ke game saat menu terbuka. */
    static boolean hideInMenu(Context context) {
        int cached = hideInMenu;
        if (cached >= 0) return cached == 1;
        boolean loaded = prefs(context).getBoolean(KEY_HIDE_IN_MENU, true);
        hideInMenu = loaded ? 1 : 0;
        return loaded;
    }

    static void setHideInMenu(Context context, boolean hide) {
        hideInMenu = hide ? 1 : 0;
        prefs(context).edit().putBoolean(KEY_HIDE_IN_MENU, hide).apply();
    }
}
