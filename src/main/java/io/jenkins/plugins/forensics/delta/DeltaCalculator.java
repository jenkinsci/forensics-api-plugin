package io.jenkins.plugins.forensics.delta;

import edu.hm.hafner.util.FilteredLog;
import io.jenkins.plugins.forensics.delta.model.Delta;

import java.io.Serializable;
import java.util.Optional;

/**
 * Calculates the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
public abstract class DeltaCalculator implements Serializable {

    private static final long serialVersionUID = 8641535877389921937L;

    /**
     * Calculates the {@link Delta} between two passed commits.
     *
     * @param currentCommitId
     *         The currently processed commit ID
     * @param referenceCommitId
     *         The reference commit ID
     * @param logger
     *         The used log
     *
     * @return the delta if it could be calculated
     */
    public abstract Optional<Delta> calculateDelta(final String currentCommitId, final String referenceCommitId,
            final FilteredLog logger);

    /**
     * A delta calculator that does nothing.
     */
    public static class NullDeltaCalculator extends DeltaCalculator {

        private static final long serialVersionUID = 1564285974889709821L;

        @Override
        public Optional<Delta> calculateDelta(final String currentCommitId, final String referenceCommitId,
                                              final FilteredLog logger) {
            return Optional.empty();
        }
    }
}
