package org.bitwisemadness.infinitedungeonapi.dialogue;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The dialogue content registry: code registrations plus the datapack table.
 *
 * <p>A query maps an entry key to its dialogue. The two sources are kept
 * apart so a datapack reload never drops a code registration, and a datapack
 * entry wins over a code entry for the same key, matching the suite's
 * datapack-first rule. The class holds no Mojang types, so the registry is
 * unit-testable; the datapack side is fed by {@code DialogueReloadListener}.</p>
 */
public final class Dialogues {

    private static final Map<String, DialogueEntry> CODE = new ConcurrentHashMap<>();

    private static volatile Map<String, DialogueEntry> datapack = Map.of();

    private Dialogues() {
    }

    /** Registers a code entry; replaces any earlier one for the key. */
    public static void register(String key, DialogueEntry entry) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(entry, "entry");
        CODE.put(key, entry);
    }

    /** Removes a code registration. */
    public static void remove(String key) {
        if (key != null) {
            CODE.remove(key);
        }
    }

    /** Replaces the datapack table, for example on reload. */
    public static void setDatapack(Map<String, DialogueEntry> entries) {
        Map<String, DialogueEntry> copy = new TreeMap<>();
        if (entries != null) {
            entries.forEach((key, entry) -> {
                if (key != null && entry != null) {
                    copy.put(key, entry);
                }
            });
        }
        datapack = Collections.unmodifiableMap(copy);
    }

    /** Drops the datapack table, leaving code registrations in place. */
    public static void clearDatapack() {
        datapack = Map.of();
    }

    /** The dialogue for a key, when one is registered. */
    public static Optional<DialogueEntry> get(String key) {
        if (key == null) {
            return Optional.empty();
        }
        DialogueEntry fromData = datapack.get(key);
        return fromData != null ? Optional.of(fromData)
                : Optional.ofNullable(CODE.get(key));
    }

    /** Every known entry, datapack entries winning, sorted by key. */
    public static Map<String, DialogueEntry> all() {
        Map<String, DialogueEntry> merged = new TreeMap<>(CODE);
        merged.putAll(datapack);
        return Collections.unmodifiableMap(merged);
    }

    /** Clears both sources; used by reload teardown and tests. */
    public static void reset() {
        CODE.clear();
        datapack = Map.of();
    }
}