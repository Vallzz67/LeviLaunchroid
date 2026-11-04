package org.levimc.pojavcontrols;

import android.view.MotionEvent;

/**
 * Menghapus jari tertentu dari sebuah MotionEvent tanpa merusak urutan DOWN/UP jari lainnya.
 * Dipakai supaya jari yang mengetuk hotbar tidak ikut masuk ke tombol/kamera overlay.
 */
final class PointerFilter {
    private PointerFilter() {}

    /**
     * Menghitung aksi event baru setelah sebagian jari dibuang.
     *
     * @param maskedAction   aksi asli (getActionMasked)
     * @param newActionIndex posisi jari penyebab aksi di dalam daftar jari yang dipertahankan,
     *                       atau -1 kalau jari itu ikut dibuang
     * @param keptCount      jumlah jari yang dipertahankan (minimal 1)
     */
    static int mapAction(int maskedAction, int newActionIndex, int keptCount) {
        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
                return newActionIndex >= 0 ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_MOVE;
            case MotionEvent.ACTION_UP:
                return newActionIndex >= 0 ? MotionEvent.ACTION_UP : MotionEvent.ACTION_MOVE;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (newActionIndex < 0) return MotionEvent.ACTION_MOVE;
                if (keptCount == 1) return MotionEvent.ACTION_DOWN;
                return MotionEvent.ACTION_POINTER_DOWN
                        | (newActionIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            case MotionEvent.ACTION_POINTER_UP:
                if (newActionIndex < 0) return MotionEvent.ACTION_MOVE;
                if (keptCount == 1) return MotionEvent.ACTION_UP;
                return MotionEvent.ACTION_POINTER_UP
                        | (newActionIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            default:
                return maskedAction;
        }
    }

    /** Mengembalikan salinan event tanpa jari di swallowedBits (bit = pointerId), atau null kalau semua dibuang. */
    static MotionEvent without(MotionEvent event, int swallowedBits) {
        int count = event.getPointerCount();
        int actionIndex = event.getActionIndex();
        int[] keep = new int[count];
        int kept = 0;
        int newActionIndex = -1;
        for (int i = 0; i < count; i++) {
            if ((swallowedBits & (1 << event.getPointerId(i))) != 0) continue;
            if (i == actionIndex) newActionIndex = kept;
            keep[kept++] = i;
        }
        if (kept == 0) return null;
        int newAction = mapAction(event.getActionMasked(), newActionIndex, kept);
        MotionEvent.PointerProperties[] props = new MotionEvent.PointerProperties[kept];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[kept];
        for (int k = 0; k < kept; k++) {
            props[k] = new MotionEvent.PointerProperties();
            event.getPointerProperties(keep[k], props[k]);
            coords[k] = new MotionEvent.PointerCoords();
            event.getPointerCoords(keep[k], coords[k]);
        }
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), newAction, kept,
                props, coords, event.getMetaState(), event.getButtonState(),
                event.getXPrecision(), event.getYPrecision(), event.getDeviceId(),
                event.getEdgeFlags(), event.getSource(), event.getFlags());
    }
}
