package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;

public abstract class RepositoryMiner implements Serializable {
    private static final long serialVersionUID = -8878714986510536182L;

    public abstract RepositoryStatistics mine(Collection<String> paths) throws InterruptedException;

    /**
     * A repository miner that does nothing.
     */
    public static class NullMiner extends RepositoryMiner {
        private static final long serialVersionUID = 6235885974889709821L;

        @Override
        public RepositoryStatistics mine(final Collection<String> paths) {
            return new RepositoryStatistics();
        }
    }
}
