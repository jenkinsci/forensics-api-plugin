package io.jenkins.plugins.forensics.reference;

import edu.hm.hafner.util.FilteredLog;

import java.util.Optional;

import hudson.model.Run;

/**
 * A small wrapper around the {@link ReferenceBuild} action to provide consumers a simple API to obtain a reference
 * build.
 *
 * @author Ullrich Hafner
 */
public class ReferenceFinder {
    /**
     * Tries to find a reference build using a registered {@link ReferenceBuild} instance.
     *
     * @param build
     *         the current build to get the reference build for
     * @param log
     *         a logger
     *
     * @return the recorded reference build if available
     */
    public Optional<Run<?, ?>> findReference(final Run<?, ?> build, final FilteredLog log) {
        var action = build.getAction(ReferenceBuild.class);
        if (action == null) {
            log.logInfo("Reference build recorder is not configured");
        }
        else {
            log.logInfo("Obtaining reference build from reference recorder");
            Optional<Run<?, ?>> referenceBuild = action.getReferenceBuild();
            if (referenceBuild.isPresent()) {
                Run<?, ?> reference = referenceBuild.get();
                log.logInfo("-> Found '%s'", reference.getFullDisplayName());

                return Optional.of(reference);
            }
            log.logInfo("-> No reference build recorded");
        }
        return Optional.empty();
    }
}
