package org.bitwisemadness.infinitedungeonapi.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.junit.jupiter.api.Test;

class DataOrderTest {

    @Test
    void sortsMapByKeyRegardlessOfInsertionOrder() {
        Map<String, Integer> scrambled = new LinkedHashMap<>();
        scrambled.put("zoo:beta", 2);
        scrambled.put("aaa:alpha", 1);
        scrambled.put("mmm:gamma", 3);

        SortedMap<String, Integer> sorted = DataOrder.sorted(scrambled);

        assertEquals(List.of("aaa:alpha", "mmm:gamma", "zoo:beta"),
                List.copyOf(sorted.keySet()));
        assertEquals(1, sorted.get("aaa:alpha"));
    }

    @Test
    void sortedViewIsUnmodifiable() {
        SortedMap<String, Integer> sorted = DataOrder.sorted(Map.of("a:a", 1));
        assertThrows(UnsupportedOperationException.class, () -> sorted.put("b:b", 2));
    }

    @Test
    void sortedUniqueDeduplicatesAndOrders() {
        assertEquals(List.of("a:a", "b:b", "c:c"),
                DataOrder.sortedUnique(List.of("c:c", "a:a", "b:b", "a:a")));
    }
}
