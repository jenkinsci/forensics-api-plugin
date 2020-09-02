package io.jenkins.plugins.forensics.miner;

import java.util.Set;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

/**
 * Queries the repository statistics of a build for a subselection of results.
 *
 * @author Ullrich Hafner
 */
public class MinerService {
    static final String NO_MINER_CONFIGURED_ERROR = "Repository miner is not configured, skipping mining";

    /**
     * Queries the statistics for the selected files of the aggregated repository statistics of the specified build.
     *
     * @param build
     *         the build
     * @param files
     *         the files to get the statistics for
     * @param log
     *         the logger
     *
     * @return the statistics for the selected files, if available
     */
    public RepositoryStatistics queryStatisticsFor(final Run<?, ?> build, final Set<String> files,
            final FilteredLog log) {
        RepositoryStatistics selected = new RepositoryStatistics();

        ForensicsBuildAction action = build.getAction(ForensicsBuildAction.class);
        if (action == null) {
            log.logInfo(NO_MINER_CONFIGURED_ERROR);

            return selected;
        }

        RepositoryStatistics everything = action.getResult();
        for (String file : files) {
            if (everything.contains(file)) {
                selected.add(everything.get(file));
            }
            else {
                log.logError("No statistics found for file '%s'", file);
            }
        }
        return selected;
    }
}
