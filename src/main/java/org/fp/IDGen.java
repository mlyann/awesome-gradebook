package org.fp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public final class IDGen {
    private static final Map<String, Queue<String>> ID_POOLS = new ConcurrentHashMap<>();

    private IDGen() {}

    /**
     * Initializes a unique ID pool for a given prefix.
     * Each ID will be used once and only once.
     */
    public static void initPool(String prefix, int count) {
        if (ID_POOLS.containsKey(prefix)) return;

        LinkedList<String> pool = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            pool.add(prefix + String.format("%02d", i)); // e.g., ASG001
        }

        Collections.shuffle(pool);
        ID_POOLS.put(prefix, pool);
    }

    /**
     * Generates a new, unused ID for the specified prefix.
     * Throws an exception if the pool is exhausted.
     */
    public static String generate(String prefix) {
        Queue<String> pool = ID_POOLS.get(prefix);
        if (pool == null) {
            throw new IllegalStateException("ID pool not initialized for prefix: " + prefix);
        }
        String id = pool.poll();
        if (id == null) {
            throw new IllegalStateException("No more available IDs for prefix: " + prefix);
        }
        return id;
    }
}