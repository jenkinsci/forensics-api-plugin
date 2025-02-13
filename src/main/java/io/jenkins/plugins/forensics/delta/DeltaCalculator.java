package io.jenkins.plugins.forensics.delta;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

import hudson.model.Run;

/**
 * Calculates the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
public abstract class DeltaCalculator implements Serializable {
    @Serial
    private static final long serialVersionUID = 8641535877389921937L;

    /**
     * Calculates the {@link Delta} between two passed Jenkins builds.
     *
     * @param build
     *         the currently processed build
     * @param referenceBuild
     *         The reference build
     * @param logger
     *         The used log
     *
     * @return the delta if it could be calculated
     */
    public Optional<Delta> calculateDelta(final Run<?, ?> build, final Run<?, ?> referenceBuild,
            final FilteredLog logger) {
        return calculateDelta(build, referenceBuild, StringUtils.EMPTY, logger);
    }

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
     * @deprecated use {@link #calculateDelta(Run, Run, FilteredLog)} instead
     */
    @Deprecated
    public abstract Optional<Delta> calculateDelta(Run<?, ?> build, Run<?, ?> referenceBuild,
            String scmKeyFilter, FilteredLog logger);

    /**
     * A delta calculator that does nothing.
     */
    public static class NullDeltaCalculator extends DeltaCalculator {
        @Serial
        private static final long serialVersionUID = 1564285974889709821L;

        @Override
        public Optional<Delta> calculateDelta(final Run<?, ?> build, final Run<?, ?> referenceBuild,
                final String scmKeyFilter, final FilteredLog logger) {
            return Optional.empty();
        }
    }
}
