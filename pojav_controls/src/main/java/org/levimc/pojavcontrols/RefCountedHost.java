package org.levimc.pojavcontrols;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Pembungkus host yang menghitung berapa tombol layar yang sedang menahan tiap tombol keyboard.
 * Dengan ini dua tombol gabungan yang sama-sama memakai Ctrl tidak saling melepas Ctrl:
 * Ctrl baru dilepas ke game setelah semua tombol yang menahannya dilepas.
 */
final class RefCountedHost implements PojavControlsHost {
    private final PojavControlsHost delegate;
    private final Map<Integer, Integer> counts = new HashMap<>();

    RefCountedHost(PojavControlsHost delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void pojavSendKey(int bedrockKeyCode, boolean down) {
        Integer current = counts.get(bedrockKeyCode);
        int count = current == null ? 0 : current;
        if (down) {
            counts.put(bedrockKeyCode, count + 1);
            if (count == 0) delegate.pojavSendKey(bedrockKeyCode, true);
        } else {
            if (count <= 0) return;
            if (count == 1) {
                counts.remove(bedrockKeyCode);
                delegate.pojavSendKey(bedrockKeyCode, false);
            } else {
                counts.put(bedrockKeyCode, count - 1);
            }
        }
    }

    /** Pengaman: lepas semua tombol yang masih tercatat tertahan. */
    synchronized void releaseStuckKeys() {
        if (counts.isEmpty()) return;
        Integer[] keys = counts.keySet().toArray(new Integer[0]);
        counts.clear();
        for (Integer key : keys) delegate.pojavSendKey(key, false);
    }

    @Override public void pojavSendMouseButton(int androidButton, boolean down) { delegate.pojavSendMouseButton(androidButton, down); }
    @Override public void pojavSendScroll(float vertical) { delegate.pojavSendScroll(vertical); }
    @Override public void pojavSendLookDelta(float deltaX, float deltaY) { delegate.pojavSendLookDelta(deltaX, deltaY); }
    @Override public void pojavSendPointer(float x, float y) { delegate.pojavSendPointer(x, y); }
    @Override public boolean pojavSendTouch(MotionEvent event) { return delegate.pojavSendTouch(event); }
    @Override public void pojavShowKeyboard() { delegate.pojavShowKeyboard(); }
    @Override public boolean pojavIsMenuOpen() { return delegate.pojavIsMenuOpen(); }
}
