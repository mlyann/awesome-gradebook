// File: IDGen.java
package org.fp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IDGen uses the Flyweight Pattern to manage unique ID generators
 * for different object types (prefixes).
 * The IDGen class is thread-safe and uses ConcurrentHashMap
 */
public final class IDGen {

    // Flyweight pool (Course Content: Small, Unique, Immutable and Reusable)
    private static final Map<String, SequentialIDFlyweight> flyweightPool =
            new ConcurrentHashMap<>();

    private IDGen() {} // prevent instantiation

    /**
     * Generate a new unique ID for the given prefix.
     */
    public static String generate(String prefix) {
        return getFlyweight(prefix).generateID();
    }

    /**
     * Initialize the next counter value for a given prefix.
     * Must be called after loading existing entities from persistence,
     * before any calls to generate(prefix).
     *
     * @param prefix    the ID prefix ("STU", "CRS", "ASG")
     * @param nextValue the next integer value (base-0) to use
     *                  (max existing number + 1)
     */
    public static void initialize(String prefix, int nextValue) {
        flyweightPool.put(prefix, new SequentialIDFlyweight(prefix, nextValue));
    }

    /** Internal flyweight implementation */
    private static class SequentialIDFlyweight {
        private final String prefix;
        private final AtomicInteger counter;

        /** Initial counter starts from 0 */
        public SequentialIDFlyweight(String prefix) {
            this(prefix, 0);
        }

        /** Initial counter starts from the given value */
        public SequentialIDFlyweight(String prefix, int initialValue) {
            this.prefix = prefix;
            this.counter = new AtomicInteger(initialValue);
        }

        /** Generates a formatted ID and increments the counter */
        public String generateID() {
            return prefix + String.format("%05d", counter.getAndIncrement());
        }
    }

    /** Returns the flyweight for the given ID prefix */
    private static SequentialIDFlyweight getFlyweight(String prefix) {
        return flyweightPool.computeIfAbsent(
                prefix,
                p -> new SequentialIDFlyweight(p)
        );
    }
}
