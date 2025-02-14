package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.util.FilteredLog;

import java.io.Serial;
import java.io.Serializable;

/**
 * Obtains commit statistics for a source code repository. Computation of the commit statistics should be done
 * incrementally, if supported by the underlying SCM  (i.e., only commits new in the current build should be
 * inspected).
 *
 * @author Ullrich Hafner
 */
public abstract class RepositoryMiner implements Serializable {
    @Serial
    private static final long serialVersionUID = -8878714986510536182L;

    /**
     * Obtains commit statistics for a source code repository.
     *
     * @param previousStatistics
     *         the repository statistics of the previous build - if there is no such build then an empty instance will
     *         be provided
     * @param logger
     *         the logger to use
     *
     * @return the aggregated statistics containing the commit statistics for the current build and the previous builds
     * @throws InterruptedException
     *         if the user canceled the processing
     */
    public abstract RepositoryStatistics mine(RepositoryStatistics previousStatistics, FilteredLog logger)
            throws InterruptedException;

    /**
     * A repository miner that does nothing.
     */
    public static class NullMiner extends RepositoryMiner {
        @Serial
        private static final long serialVersionUID = 6235885974889709821L;

        @Override
        public RepositoryStatistics mine(final RepositoryStatistics previousStatistics, final FilteredLog logger) {
            return previousStatistics;
        }
    }
}
