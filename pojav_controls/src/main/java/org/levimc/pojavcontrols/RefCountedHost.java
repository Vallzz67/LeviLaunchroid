package org.levimc.pojavcontrols;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Pembungkus host untuk tombol keyboard dan klik mouse:
 *  - Refcount: dua tombol layar yang sama-sama menekan Ctrl tidak saling melepas Ctrl.
 *    Ctrl baru dilepas ke game setelah semua tombol yang menahannya dilepas.
 *  - Lama tahan minimum: kalau ketukan sangat singkat (tekan dan lepas dalam kurang dari
 *    beberapa milidetik), pelepasannya ditunda supaya game sempat membaca tombol itu
 *    sedang ditekan. Tanpa ini sebagian klik/serangan tidak terbaca game.
 */
final class RefCountedHost implements PojavControlsHost {
    interface Emitter { void emit(boolean down); }

    interface HoldSource { int minHoldMs(); }

    interface Scheduler {
        long now();
        void postDelayed(Runnable runnable, long delayMs);
        void cancel(Runnable runnable);
    }

    private static final class AndroidScheduler implements Scheduler {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public long now() { return SystemClock.uptimeMillis(); }
        @Override public void postDelayed(Runnable runnable, long delayMs) { handler.postDelayed(runnable, delayMs); }
        @Override public void cancel(Runnable runnable) { handler.removeCallbacks(runnable); }
    }

    private static final int MOUSE_ID_BASE = 1_000_000;

    private static final class Held {
        int count;
        long downAt;
        Emitter emitter;
    }

    private static final class Pending {
        Runnable runnable;
        Emitter emitter;
    }

    private final PojavControlsHost delegate;
    private final HoldSource holdSource;
    private final Scheduler scheduler;
    private final Map<Integer, Held> held = new HashMap<>();
    private final Map<Integer, Pending> pending = new HashMap<>();

    RefCountedHost(PojavControlsHost delegate, Context context) {
        this(delegate, () -> ControlSettings.minHoldMs(context), new AndroidScheduler());
    }

    RefCountedHost(PojavControlsHost delegate, HoldSource holdSource, Scheduler scheduler) {
        this.delegate = delegate;
        this.holdSource = holdSource;
        this.scheduler = scheduler;
    }

    @Override
    public void pojavSendKey(int bedrockKeyCode, boolean down) {
        handle(bedrockKeyCode, down, d -> delegate.pojavSendKey(bedrockKeyCode, d));
    }

    @Override
    public void pojavSendMouseButton(int androidButton, boolean down) {
        handle(MOUSE_ID_BASE + androidButton, down,
                d -> delegate.pojavSendMouseButton(androidButton, d));
    }

    private synchronized void handle(int id, boolean down, Emitter emitter) {
        if (down) {
            flushPending(id);  // ketukan baru saat pelepasan sebelumnya masih tertunda
            Held h = held.get(id);
            if (h == null) {
                h = new Held();
                h.emitter = emitter;
                held.put(id, h);
            }
            if (h.count++ == 0) {
                h.downAt = scheduler.now();
                emitter.emit(true);
            }
            return;
        }
        Held h = held.get(id);
        if (h == null || h.count <= 0) return;
        if (--h.count > 0) return;
        held.remove(id);
        long minHold = Math.max(0, holdSource.minHoldMs());
        long remaining = minHold - (scheduler.now() - h.downAt);
        if (remaining <= 0) {
            emitter.emit(false);
            return;
        }
        final Pending p = new Pending();
        p.emitter = emitter;
        p.runnable = () -> {
            synchronized (RefCountedHost.this) {
                if (pending.get(id) == p) {
                    pending.remove(id);
                    p.emitter.emit(false);
                }
            }
        };
        pending.put(id, p);
        scheduler.postDelayed(p.runnable, remaining);
    }

    private void flushPending(int id) {
        Pending p = pending.remove(id);
        if (p == null) return;
        scheduler.cancel(p.runnable);
        p.emitter.emit(false);
    }

    /** Pengaman: lepas semua tombol yang masih tertahan atau menunggu dilepas. */
    synchronized void releaseStuckKeys() {
        for (Integer id : new ArrayList<>(pending.keySet())) flushPending(id);
        for (Held h : new ArrayList<>(held.values())) h.emitter.emit(false);
        held.clear();
    }

    @Override public void pojavSendScroll(float vertical) { delegate.pojavSendScroll(vertical); }
    @Override public void pojavSendLookDelta(float deltaX, float deltaY) { delegate.pojavSendLookDelta(deltaX, deltaY); }
    @Override public void pojavSendPointer(float x, float y) { delegate.pojavSendPointer(x, y); }
    @Override public boolean pojavSendTouch(MotionEvent event) { return delegate.pojavSendTouch(event); }
    @Override public void pojavShowKeyboard() { delegate.pojavShowKeyboard(); }
    @Override public boolean pojavIsMenuOpen() { return delegate.pojavIsMenuOpen(); }
}
