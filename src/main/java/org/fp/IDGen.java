// File: IDGen.java
package org.fp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IDGen uses the Flyweight Pattern to manage unique ID generators
 * for different object types (prefixes).
 * 增加 initialize(...) 方法，可在程序重启后从已有 ID 恢复计数器。
 */
public final class IDGen {

    // Flyweight pool: prefix -> counter generator
    private static final Map<String, SequentialIDFlyweight> flyweightPool =
            new ConcurrentHashMap<>();

    private IDGen() {} // prevent instantiation

    /**
     * Generate a new unique ID for the given prefix.
     * e.g., "STU00001", "ASG00010"
     */
    public static String generate(String prefix) {
        return getFlyweight(prefix).generateID();
    }

    /**
     * Initialize the next counter value for a given prefix.
     * Must be called after loading existing entities from persistence,
     * before any calls to generate(prefix).
     *
     * @param prefix    the ID prefix (e.g. "STU", "CRS", "ASG", etc.)
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

        /** 初始计数器从 0 开始 */
        public SequentialIDFlyweight(String prefix) {
            this(prefix, 0);
        }

        /** 指定初始计数器值 */
        public SequentialIDFlyweight(String prefix, int initialValue) {
            this.prefix = prefix;
            this.counter = new AtomicInteger(initialValue);
        }

        /** 生成格式化后的 ID，并自增计数器 */
        public String generateID() {
            return prefix + String.format("%05d", counter.getAndIncrement());
        }
    }

    /** 获取或新建指定前缀的 Flyweight 对象 */
    private static SequentialIDFlyweight getFlyweight(String prefix) {
        return flyweightPool.computeIfAbsent(
                prefix,
                p -> new SequentialIDFlyweight(p)
        );
    }
}
