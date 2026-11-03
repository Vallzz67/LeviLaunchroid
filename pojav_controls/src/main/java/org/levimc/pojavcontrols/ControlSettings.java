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
    static final int MIN_HOLD_MIN = 0;
    static final int MIN_HOLD_MAX = 150;
    static final int MIN_HOLD_DEFAULT = 50;
    static final int HOTBAR_W_MIN = 15;
    static final int HOTBAR_W_MAX = 100;
    static final int HOTBAR_W_DEFAULT = 55;
    static final int HOTBAR_H_MIN = 5;
    static final int HOTBAR_H_MAX = 40;
    static final int HOTBAR_H_DEFAULT = 16;

    private static final String PREFS = "pojav_controls_settings";
    private static final String KEY_SENS = "look_sensitivity_percent";
    private static final String KEY_HIDE_IN_MENU = "hide_controls_in_menu";
    private static final String KEY_CURSOR = "cursor_speed_percent";
    private static final String KEY_CURSOR_SIZE = "cursor_size_percent";
    private static final String KEY_COMBO_DELAY = "combo_delay_ms";
    private static final String KEY_CURSOR_IN_MENUS = "cursor_in_menus_v2";
    private static final String KEY_MIN_HOLD = "min_click_hold_ms";
    private static final String KEY_HOTBAR_PASS = "hotbar_passthrough";
    private static final String KEY_HOTBAR_W = "hotbar_w_percent";
    private static final String KEY_HOTBAR_H = "hotbar_h_percent";
    private static final String KEY_HOTBAR_GUIDE = "hotbar_guide";

    private static volatile int sensitivityPercent = -1;
    private static volatile int cursorSpeedPercent = -1;
    private static volatile int cursorSizePercent = -1;
    private static volatile int comboDelayMs = -1;
    private static volatile int cursorInMenus = -1;  // -1 = belum dibaca
    private static volatile int minHoldMs = -1;
    private static volatile int hotbarPass = -1;
    private static volatile int hotbarW = -1;
    private static volatile int hotbarH = -1;
    private static volatile int hotbarGuide = -1;
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
     * Kalau aktif, kursor virtual muncul dan dipakai saat menu/inventory/pengaturan terbuka
     * (tidak pernah di dalam game). Kalau mati (bawaan), item di menu disentuh langsung.
     */
    static boolean cursorInMenus(Context context) {
        int cached = cursorInMenus;
        if (cached >= 0) return cached == 1;
        boolean loaded = prefs(context).getBoolean(KEY_CURSOR_IN_MENUS, false);
        cursorInMenus = loaded ? 1 : 0;
        return loaded;
    }

    static void setCursorInMenus(Context context, boolean enabled) {
        cursorInMenus = enabled ? 1 : 0;
        prefs(context).edit().putBoolean(KEY_CURSOR_IN_MENUS, enabled).apply();
    }

    /** Lama minimum (ms) sebuah klik/tombol ditahan sebelum dilepas, supaya game tidak melewatkannya. */
    static int minHoldMs(Context context) {
        int cached = minHoldMs;
        if (cached >= 0) return cached;
        int loaded = Math.max(MIN_HOLD_MIN, Math.min(MIN_HOLD_MAX,
                prefs(context).getInt(KEY_MIN_HOLD, MIN_HOLD_DEFAULT)));
        minHoldMs = loaded;
        return loaded;
    }

    static void setMinHoldMs(Context context, int ms) {
        int value = Math.max(MIN_HOLD_MIN, Math.min(MIN_HOLD_MAX, ms));
        minHoldMs = value;
        prefs(context).edit().putInt(KEY_MIN_HOLD, value).apply();
    }

    /** Kalau aktif, sentuhan di area hotbar (bawah-tengah layar) di dalam game langsung ke game. */
    static boolean hotbarPassThrough(Context context) {
        int cached = hotbarPass;
        if (cached >= 0) return cached == 1;
        boolean loaded = prefs(context).getBoolean(KEY_HOTBAR_PASS, true);
        hotbarPass = loaded ? 1 : 0;
        return loaded;
    }

    static void setHotbarPassThrough(Context context, boolean enabled) {
        hotbarPass = enabled ? 1 : 0;
        prefs(context).edit().putBoolean(KEY_HOTBAR_PASS, enabled).apply();
    }

    static int hotbarWidthPercent(Context context) {
        int cached = hotbarW;
        if (cached >= 0) return cached;
        int loaded = Math.max(HOTBAR_W_MIN, Math.min(HOTBAR_W_MAX,
                prefs(context).getInt(KEY_HOTBAR_W, HOTBAR_W_DEFAULT)));
        hotbarW = loaded;
        return loaded;
    }

    static void setHotbarWidthPercent(Context context, int percent) {
        int value = Math.max(HOTBAR_W_MIN, Math.min(HOTBAR_W_MAX, percent));
        hotbarW = value;
        prefs(context).edit().putInt(KEY_HOTBAR_W, value).apply();
    }

    static int hotbarHeightPercent(Context context) {
        int cached = hotbarH;
        if (cached >= 0) return cached;
        int loaded = Math.max(HOTBAR_H_MIN, Math.min(HOTBAR_H_MAX,
                prefs(context).getInt(KEY_HOTBAR_H, HOTBAR_H_DEFAULT)));
        hotbarH = loaded;
        return loaded;
    }

    static void setHotbarHeightPercent(Context context, int percent) {
        int value = Math.max(HOTBAR_H_MIN, Math.min(HOTBAR_H_MAX, percent));
        hotbarH = value;
        prefs(context).edit().putInt(KEY_HOTBAR_H, value).apply();
    }

    /** Menampilkan kotak kuning area hotbar di dalam game, untuk membantu mengatur ukurannya. */
    static boolean showHotbarGuide(Context context) {
        int cached = hotbarGuide;
        if (cached >= 0) return cached == 1;
        boolean loaded = prefs(context).getBoolean(KEY_HOTBAR_GUIDE, false);
        hotbarGuide = loaded ? 1 : 0;
        return loaded;
    }

    static void setShowHotbarGuide(Context context, boolean show) {
        hotbarGuide = show ? 1 : 0;
        prefs(context).edit().putBoolean(KEY_HOTBAR_GUIDE, show).apply();
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
