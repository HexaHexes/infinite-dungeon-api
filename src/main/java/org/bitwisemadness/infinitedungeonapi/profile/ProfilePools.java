package org.bitwisemadness.infinitedungeonapi.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bitwisemadness.infinitedungeonapi.data.ResourceRef;

/**
 * The spawn pool ids a dimension profile points at: fodder, elite, and boss.
 *
 * <p>Ids are namespaced references into the consumer's pool registry - the core
 * module's datapack pools. This library cannot know which pools exist, so it
 * validates the reference shape only; resolving an unknown id against a pool
 * registry belongs to the consumer that owns that registry. A malformed id is
 * dropped rather than failing a load, so a profile can mention optional
 * content.</p>
 *
 * @param fodder the ordinary-mob pool id
 * @param elite  the elite pool id
 * @param boss   the guardians' pool id
 */
public record ProfilePools(Optional<String> fodder,
                           Optional<String> elite,
                           Optional<String> boss) {

    public ProfilePools {
        fodder = normalise(fodder);
        elite = normalise(elite);
        boss = normalise(boss);
    }

    /** A pool set that names nothing. */
    public static ProfilePools empty() {
        return new ProfilePools(Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** The present ids, in a stable order (fodder, elite, boss). */
    public List<String> ids() {
        List<String> ids = new ArrayList<>(3);
        fodder.ifPresent(ids::add);
        elite.ifPresent(ids::add);
        boss.ifPresent(ids::add);
        return List.copyOf(ids);
    }

    /** The present ids that are not well-formed resource locations. */
    public List<String> malformedIds() {
        List<String> malformed = new ArrayList<>();
        for (String id : ids()) {
            if (!isWellFormed(id)) {
                malformed.add(id);
            }
        }
        return List.copyOf(malformed);
    }

    /** A copy with malformed ids dropped. */
    public ProfilePools sanitised() {
        return new ProfilePools(
                presentWhenWellFormed(fodder),
                presentWhenWellFormed(elite),
                presentWhenWellFormed(boss));
    }

    private static Optional<String> presentWhenWellFormed(Optional<String> id) {
        return id.filter(ProfilePools::isWellFormed);
    }

    private static Optional<String> normalise(Optional<String> id) {
        if (id == null) {
            return Optional.empty();
        }
        return id.map(String::trim).filter(value -> !value.isEmpty());
    }

    private static boolean isWellFormed(String id) {
        // A pool is a plain id, never a tag reference.
        return ResourceRef.tryParse(id).filter(ref -> !ref.tag()).isPresent();
    }
}
