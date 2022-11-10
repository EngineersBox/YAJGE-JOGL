package com.engineersbox.yajgejogl.util;

import java.util.Map;
import java.util.function.BiConsumer;

public class IteratorUtils {

    private IteratorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <K, V> void forEach(final Map<K, V> map, final BiConsumer<K, V> consumer) {
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

}
