package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

/**
 * Obtains commit statistics for a source code repository. Computation of the commit statistics should be done
 * incrementally, if supported by the underlying SCM  (i.e., only commits new in the current build should be
 * inspected).
 *
 * @author Ullrich Hafner
 */
public abstract class RepositoryMiner implements Serializable {
    private static final long serialVersionUID = -8878714986510536182L;

    /**
     * Obtains commit statistics for a source code repository. If the {@code previousStatistics} are filled with results
     * of a previous build, then an incremental mining will be started that only inspects the additional commits from
     * the current build. Otherwise, the whole repository will be mined (which might take some time for large
     * repositories).
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
     * Obtains delta commit statistics for a source code repository. Starts an incremental mining from the latest commit
     * from the current build up to the common ancestor commit with the provided {@code referenceBuild}.
     *
     * @param referenceBuild
     *         the reference build to use
     * @param logger
     *         the logger to use
     *
     * @return the statistics containing the commit statistics for the current build up to the common ancestor with the
     *         reference build
     * @throws InterruptedException
     *         if the user canceled the processing
     * @since 1.2.0
     */
    public RepositoryStatistics mine(final Run<?, ?> referenceBuild, final FilteredLog logger) throws InterruptedException {
        return new RepositoryStatistics();
    }

    /**
     * A repository miner that does nothing.
     */
    public static class NullMiner extends RepositoryMiner {
        private static final long serialVersionUID = 6235885974889709821L;

        @Override
        public RepositoryStatistics mine(final RepositoryStatistics previousStatistics, final FilteredLog logger) {
            return previousStatistics;
        }
    }
}
