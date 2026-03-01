package org.vmstudio.visor.api.common.utils;


import org.joml.Quaternionf;

import java.util.*;

public class QuaternionFloatHistory {
    private final Deque<Entry> history;
    private final int capacity;

    /**
     * @param capacity maximum number of entries to keep
     */
    public QuaternionFloatHistory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.capacity = capacity;
        this.history = new ArrayDeque<>(capacity);
    }

    /**
     * Add a new orientation sample, dropping the oldest if we're at capacity.
     */
    public synchronized void add(Quaternionf quat) {

        history.addLast(new Entry(new Quaternionf(quat), System.currentTimeMillis()));
        if (history.size() > capacity) {
            history.removeFirst();
        }
    }

    /** Clear the history. */
    public synchronized void clear() {
        history.clear();
    }

    /**
     * @return the most recently added quaternion
     * @throws NoSuchElementException if no samples exist
     */
    public synchronized Quaternionf latest() {
        Entry last = history.peekLast();
        if (last == null) throw new NoSuchElementException("No rotations in history");
        return new Quaternionf(last.data);
    }

    /**
     * Compute the (spherical) average of all samples within the past N seconds.
     * If none are recent enough, returns the latest.
     *
     * @param seconds lookback window in seconds
     */
    public synchronized Quaternionf averageRotation(double seconds) {
        long now = System.currentTimeMillis();
        List<Quaternionf> recent = new ArrayList<>();
        for (Iterator<Entry> it = history.descendingIterator(); it.hasNext(); ) {
            Entry e = it.next();
            if (now - e.timestamp > (long)(seconds * 1_000)) break;
            recent.add(e.data);
        }

        if (recent.isEmpty()) {
            return latest();
        }

        // Build normalized weights that sum to 1
        int n = recent.size();
        float[] weights = new float[n];
        float w = 1.0f / n;
        Arrays.fill(weights, w);


        Quaternionf result = new Quaternionf();
        Quaternionf.slerp(recent.toArray(new Quaternionf[0]), weights, result);
        return result;
    }

    private record Entry(Quaternionf data, long timestamp) { }
}
