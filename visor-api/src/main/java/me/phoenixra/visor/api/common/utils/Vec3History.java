package me.phoenixra.visor.api.common.utils;

import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.LongSupplier;

public class Vec3History {
    private final Deque<Entry> history;
    private final int capacity;
    private final LongSupplier clock;

    /**
     * @param capacity max samples to retain
     * @param clock    source of 'now' timestamps (e.g. System::currentTimeMillis or Util::getMillis)
     */
    public Vec3History(int capacity, LongSupplier clock) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.history = new ArrayDeque<>(capacity);
        this.clock = clock;
    }

    public Vec3History(int capacity) {
        this(capacity, Util::getMillis);
    }

    /** Add a new position sample, evicting the oldest if we exceed capacity. */
    public synchronized void add(Vec3 pos) {
        history.addLast(new Entry(pos, clock.getAsLong()));
        if (history.size() > capacity) {
            history.removeFirst();
        }
    }

    /** Remove all stored samples. */
    public synchronized void clear() {
        history.clear();
    }

    /**
     * @return the most recent sample
     * @throws IllegalStateException if no samples exist
     */
    public synchronized Vec3 latest() {
        Entry last = history.peekLast();
        if (last == null) throw new IllegalStateException("No history available");
        return last.pos;
    }

    /**
     * @return total path‐length traveled over the last `seconds`
     */
    public synchronized double totalMovement(double seconds) {
        List<Entry> recent = getRecent(seconds);
        if (recent.size() < 2) return 0.0;

        double sum = 0.0;
        for (int i = 1; i < recent.size(); i++) {
            sum += recent.get(i).pos.distanceTo(recent.get(i-1).pos);
        }
        return sum;
    }

    /**
     * @return net displacement vector from oldest->newest sample in the last `seconds`
     */
    public synchronized Vec3 netMovement(double seconds) {
        List<Entry> recent = getRecent(seconds);
        if (recent.size() < 2) return Vec3.ZERO;
        Vec3 first = recent.get(0).pos;
        Vec3 last = recent.get(recent.size()-1).pos;
        return last.subtract(first);
    }

    /**
     * @return average speed (distance/time) over each segment in the last `seconds`
     */
    public synchronized double averageSpeed(double seconds) {
        List<Entry> recent = getRecent(seconds);
        if (recent.size() < 2) return 0.0;

        double sumSpeeds = 0.0;
        int segments  = 0;
        for (int i = 1; i < recent.size(); i++) {
            Entry prev = recent.get(i-1), curr = recent.get(i);
            double dtSeconds = (curr.timestamp - prev.timestamp) / 1000.0;
            if (dtSeconds > 0) {
                double dist = curr.pos.distanceTo(prev.pos);
                sumSpeeds += dist / dtSeconds;
                segments++;
            }
        }
        return segments == 0 ? 0.0 : sumSpeeds / segments;
    }

    /**
     * @return arithmetic mean of all positions over the last `seconds`
     */
    public synchronized Vec3 averagePosition(double seconds) {
        List<Entry> recent = getRecent(seconds);
        if (recent.isEmpty()) return Vec3.ZERO;

        double x=0,y=0,z=0;
        for (Entry e : recent) {
            x += e.pos.x;
            y += e.pos.y;
            z += e.pos.z;
        }
        double inv = 1.0 / recent.size();
        return new Vec3(x*inv, y*inv, z*inv);
    }

    /**
     * Helper: walk backward from newest->oldest until timestamp < now - seconds*1000.
     * Returns the remaining list in 'chronological' order (oldest first).
     */
    private List<Entry> getRecent(double seconds) {
        long now = clock.getAsLong();
        long cutoff = now - (long)(seconds * 1_000);
        List<Entry> out = new ArrayList<>(capacity);
        for (Iterator<Entry> it = history.descendingIterator(); it.hasNext(); ) {
            Entry e = it.next();
            if (e.timestamp < cutoff) break;
            out.add(0, e);  // prepend so result is oldest→newest
        }
        return out;
    }

        private record Entry(Vec3 pos, long timestamp) { }
}