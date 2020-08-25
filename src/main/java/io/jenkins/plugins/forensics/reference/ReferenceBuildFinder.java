package io.jenkins.plugins.forensics.reference;

import java.util.Optional;

import hudson.model.Run;

/**
 * Provides a reference build for a given build. TODO: add detailed description
 *
 * @author Ullrich Hafner
 */
// TODO: for MultiBranch jobs there should be no need to call the recorder
public class ReferenceBuildFinder {
    /**
     * Tries to find a reference build.
     *
     * @param run
     *         the build to find the reference for
     *
     * @return the reference build, if found
     */
    public Optional<Run<?, ?>> find(final Run<?, ?> run) {
        ReferenceBuild action = run.getAction(ReferenceBuild.class);
        if (action == null) {
            // TODO: try to automatically find a reference build for multi branch pipelines
            return Optional.empty();
        }
        return action.getReferenceBuild();
    }
}
