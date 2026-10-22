package org.bitwisemadness.infinitedungeonapi.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Deterministic ordering for datapack data.
 *
 * <p>Every suite data type is keyed by its file location and processed in
 * sorted order, so two packs with identical content produce identical results
 * regardless of filesystem enumeration order.</p>
 */
public final class DataOrder {

    private DataOrder() {
    }

    /** Returns an unmodifiable map iteration-ordered by key. */
    public static <V> SortedMap<String, V> sorted(Map<String, V> entries) {
        return Collections.unmodifiableSortedMap(new TreeMap<>(entries));
    }

    /** Returns the keys in sorted order. */
    public static <V> List<String> sortedKeys(Map<String, V> entries) {
        return List.copyOf(new TreeMap<>(entries).keySet());
    }

    /** Returns the entries in sorted, deduplicated order. */
    public static List<String> sortedUnique(Collection<String> entries) {
        return List.copyOf(new TreeSet<>(entries));
    }
}
