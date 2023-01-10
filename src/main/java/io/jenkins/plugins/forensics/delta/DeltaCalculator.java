package io.jenkins.plugins.forensics.delta;

import java.io.Serializable;
import java.util.Optional;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

/**
 * Calculates the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
public abstract class DeltaCalculator implements Serializable {
    private static final long serialVersionUID = 8641535877389921937L;

    /**
     * Calculates the {@link Delta} between two passed Jenkins builds.
     *
     * @param build
     *         The currently processed build
     * @param referenceBuild
     *         The reference build
     * @param scmKeyFilter
     *         The SCM key filter
     * @param logger
     *         The used log
     *
     * @return the delta if it could be calculated
     */
    public abstract Optional<Delta> calculateDelta(Run<?, ?> build, Run<?, ?> referenceBuild,
            String scmKeyFilter, FilteredLog logger);

    /**
     * A delta calculator that does nothing.
     */
    public static class NullDeltaCalculator extends DeltaCalculator {
        private static final long serialVersionUID = 1564285974889709821L;

        @Override
        public Optional<Delta> calculateDelta(final Run<?, ?> build, final Run<?, ?> referenceBuild,
                final String scmKeyFilter, final FilteredLog logger) {
            return Optional.empty();
        }
    }
}
