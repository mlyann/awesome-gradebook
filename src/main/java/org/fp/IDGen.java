package org.fp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IDGen uses the Flyweight Pattern to manage unique ID generators
 * for different object types (prefixes).
 */
public final class IDGen {

    // Flyweight pool: prefix -> counter generator
    private static final Map<String, IDFlyweight> flyweightPool = new ConcurrentHashMap<>();

    private IDGen() {} // prevent instantiation

    /**
     * Generate a new unique ID for the given prefix.
     * e.g., "STU00001", "ASG00010"
     */
    public static String generate(String prefix) {
        return getFlyweight(prefix).generateID();
    }

    /**
     * Internal flyweight interface (shared logic per type)
     */
    private interface IDFlyweight {
        String generateID();
    }

    /**
     * Shared ID generator for one specific prefix.
     * Uses AtomicInteger to guarantee thread-safe uniqueness.
     */
    private static class SequentialIDFlyweight implements IDFlyweight {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(0);

        public SequentialIDFlyweight(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String generateID() {
            return prefix + String.format("%05d", counter.getAndIncrement());
        }
    }

    private static IDFlyweight getFlyweight(String prefix) {
        return flyweightPool.computeIfAbsent(prefix, SequentialIDFlyweight::new);
    }
}
