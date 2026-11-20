package org.bitwisemadness.infinitedungeonapi.entrance;

import java.util.Objects;

/**
 * The generation policy's decision for an entrance (api#58). The register's
 * "explicit, never implicit" rule is baked in: every non-generate outcome is
 * named, and the fallback behaviour comes from the caller's configured value
 * ({@link EntranceContext#fallbackGenerate()}), never guessed.
 */
public record EntranceDecision(Outcome outcome, EntranceSite site) {

    /** What the policy decided. */
    public enum Outcome {
        /** The site is suitable: generate the fixed structure there. */
        GENERATE,
        /** The entrance does not generate here (explicit fallback: skip). */
        SKIP,
        /** No provider answered and the explicit fallback is generate. */
        FALLBACK_GENERATE
    }

    public EntranceDecision {
        Objects.requireNonNull(outcome, "outcome");
    }

    /** A generate decision for the site. */
    public static EntranceDecision generate(EntranceSite site) {
        return new EntranceDecision(Outcome.GENERATE, Objects.requireNonNull(site, "site"));
    }

    /** A skip decision. */
    public static EntranceDecision skip() {
        return new EntranceDecision(Outcome.SKIP, null);
    }

    /** The explicit generate-fallback decision (no provider answered). */
    public static EntranceDecision fallbackGenerate() {
        return new EntranceDecision(Outcome.FALLBACK_GENERATE, null);
    }
}
