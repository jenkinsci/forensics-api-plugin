package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;

/**
 * Obtains commit statistics for a given collection of files.
 */
public abstract class RepositoryMiner implements Serializable {
    private static final long serialVersionUID = -8878714986510536182L;

    /**
     * Obtains statistical information for the specified files. If the collection of files is empty, then the statistics
     * for the whole repository will be returned.
     *
     * @param relativeFileNames
     *         the files (relative file names with respect to the workspace root) to gather statistics for
     *
     * @return the statistics
     * @throws InterruptedException
     *         if the user canceled the processing
     */
    public abstract RepositoryStatistics mine(Collection<String> relativeFileNames) throws InterruptedException;

    /**
     * A repository miner that does nothing.
     */
    public static class NullMiner extends RepositoryMiner {
        private static final long serialVersionUID = 6235885974889709821L;

        @Override
        public RepositoryStatistics mine(final Collection<String> relativeFileNames) {
            return new RepositoryStatistics();
        }
    }
}
