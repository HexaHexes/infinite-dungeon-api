package org.bitwisemadness.infinitedungeonapi.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Expands an id-or-tag reference into the ids it covers.
 *
 * <p>Missing ids are dropped and empty tags are tolerated, never fatal, so a
 * reference to optional content degrades to "nothing from this entry" instead
 * of failing a datapack reload. Expansion order follows the caller's tag
 * contents; sort the result when determinism matters (see {@link DataOrder}).</p>
 */
public final class TagExpansion {

    private TagExpansion() {
    }

    /**
     * Expands one reference.
     *
     * @param ref         the id or tag reference
     * @param idExists    whether a plain id resolves to a present registry entry
     * @param tagContents the members of a tag, in the caller's order
     */
    public static List<String> expand(ResourceRef ref,
                                      Predicate<String> idExists,
                                      Function<String, ? extends Collection<String>> tagContents) {
        if (!ref.tag()) {
            return idExists.test(ref.location()) ? List.of(ref.location()) : List.of();
        }
        Collection<String> members = tagContents.apply(ref.location());
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<String> found = new ArrayList<>(members.size());
        for (String member : members) {
            if (member != null && idExists.test(member)) {
                found.add(member);
            }
        }
        return List.copyOf(found);
    }

    /** Expands references in order, concatenating their members. */
    public static List<String> expandAll(Collection<ResourceRef> refs,
                                         Predicate<String> idExists,
                                         Function<String, ? extends Collection<String>> tagContents) {
        List<String> found = new ArrayList<>();
        for (ResourceRef ref : refs) {
            found.addAll(expand(ref, idExists, tagContents));
        }
        return List.copyOf(found);
    }
}
