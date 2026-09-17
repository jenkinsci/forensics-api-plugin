package io.jenkins.plugins.forensics.miner;

/**
 * Determines the baseline that is used to compute the commits that are new in the current build.
 *
 * @author Michael Trimarchi
 */
public enum Baseline {
    /**
     * Uses the latest commit of the previous build as baseline. The commits that are new in the current build are
     * determined by walking the history from {@code HEAD} up to the latest commit of the previous build.
     */
    PREVIOUS,

    /**
     * Uses the head of the target branch of a change request as baseline. The commits that are new in the current
     * build are determined by walking the history from {@code HEAD} up to the merge base of {@code HEAD} and the
     * target branch head.
     */
    TARGET
}
