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
    static final int CURSOR_SIZE_MIN = 30;
    static final int CURSOR_SIZE_MAX = 200;
    static final int CURSOR_SIZE_DEFAULT = 80;
    static final int COMBO_DELAY_MIN = 0;
    static final int COMBO_DELAY_MAX = 200;
    static final int COMBO_DELAY_DEFAULT = 0;

    private static final String PREFS = "pojav_controls_settings";
    private static final String KEY_SENS = "look_sensitivity_percent";
    private static final String KEY_HIDE_IN_MENU = "hide_controls_in_menu";
    private static final String KEY_CURSOR = "cursor_speed_percent";
    private static final String KEY_CURSOR_SIZE = "cursor_size_percent";
    private static final String KEY_COMBO_DELAY = "combo_delay_ms";
    private static final String KEY_CURSOR_IN_MENUS = "cursor_in_menus";

    private static volatile int sensitivityPercent = -1;
    private static volatile int cursorSpeedPercent = -1;
    private static volatile int cursorSizePercent = -1;
    private static volatile int comboDelayMs = -1;
    private static volatile int cursorInMenus = -1;  // -1 = belum dibaca
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

    static int cursorSizePercent(Context context) {
        int cached = cursorSizePercent;
        if (cached >= 0) return cached;
        int loaded = Math.max(CURSOR_SIZE_MIN, Math.min(CURSOR_SIZE_MAX,
                prefs(context).getInt(KEY_CURSOR_SIZE, CURSOR_SIZE_DEFAULT)));
        cursorSizePercent = loaded;
        return loaded;
    }

    static float cursorSizeFactor(Context context) {
        return cursorSizePercent(context) / 100f;
    }

    static void setCursorSizePercent(Context context, int percent) {
        int value = Math.max(CURSOR_SIZE_MIN, Math.min(CURSOR_SIZE_MAX, percent));
        cursorSizePercent = value;
        prefs(context).edit().putInt(KEY_CURSOR_SIZE, value).apply();
    }

    /** Jeda (ms) antar tombol dalam satu tombol gabungan. 0 = semua ditekan bersamaan. */
    static int comboDelayMs(Context context) {
        int cached = comboDelayMs;
        if (cached >= 0) return cached;
        int loaded = Math.max(COMBO_DELAY_MIN, Math.min(COMBO_DELAY_MAX,
                prefs(context).getInt(KEY_COMBO_DELAY, COMBO_DELAY_DEFAULT)));
        comboDelayMs = loaded;
        return loaded;
    }

    static void setComboDelayMs(Context context, int ms) {
        int value = Math.max(COMBO_DELAY_MIN, Math.min(COMBO_DELAY_MAX, ms));
        comboDelayMs = value;
        prefs(context).edit().putInt(KEY_COMBO_DELAY, value).apply();
    }

    /**
     * Kalau aktif (bawaan), kursor virtual otomatis muncul dan dipakai saat menu/inventory/pengaturan
     * terbuka, dan tidak pernah muncul di dalam game.
     */
    static boolean cursorInMenus(Context context) {
        int cached = cursorInMenus;
        if (cached >= 0) return cached == 1;
        boolean loaded = prefs(context).getBoolean(KEY_CURSOR_IN_MENUS, true);
        cursorInMenus = loaded ? 1 : 0;
        return loaded;
    }

    static void setCursorInMenus(Context context, boolean enabled) {
        cursorInMenus = enabled ? 1 : 0;
        prefs(context).edit().putBoolean(KEY_CURSOR_IN_MENUS, enabled).apply();
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
