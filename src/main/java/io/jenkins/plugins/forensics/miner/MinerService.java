package io.jenkins.plugins.forensics.miner;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

import io.jenkins.plugins.util.BuildAction;

/**
 * Queries the repository statistics of a build for a subselection of results.
 *
 * @author Ullrich Hafner
 */
public class MinerService {
    static final String NO_MINER_ERROR = "<a href=\"https://github.com/jenkinsci/forensics-api-plugin\">Repository miner</a> is not configured, skipping repository mining";

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
     * @deprecated use {@link #queryStatisticsFor(String, Run, Set, FilteredLog)}
     */
    @Deprecated
    public RepositoryStatistics queryStatisticsFor(final Run<?, ?> build, final Set<String> files,
            final FilteredLog log) {
        return queryStatisticsFor(StringUtils.EMPTY, build, files, log);
    }

    /**
     * Queries the statistics for the selected files of the aggregated repository statistics of the specified build.
     *
     * @param scm
     *         the SCM to get the results from (can be empty if there is just a single repository used)
     * @param build
     *         the build
     * @param files
     *         the files to get the statistics for
     * @param logger
     *         the logger
     *
     * @return the statistics for the selected files, if available
     */
    public RepositoryStatistics queryStatisticsFor(final String scm, final Run<?, ?> build,
            final Set<String> files, final FilteredLog logger) {
        RepositoryStatistics selected = new RepositoryStatistics();

        List<ForensicsBuildAction> actions = build.getActions(ForensicsBuildAction.class);
        if (actions.isEmpty()) {
            logger.logInfo(NO_MINER_ERROR);
            return selected;
        }

        RepositoryStatistics everything = actions.stream()
                .filter(a -> a.getScmKey().contains(scm))
                .findAny()
                .map(BuildAction::getResult)
                .orElse(new RepositoryStatistics());
        logger.logInfo("Extracting repository statistics for affected files (%d of %d)", files.size(), everything.size());

        for (String file : files) {
            if (everything.contains(file)) {
                selected.add(everything.get(file));
            }
            else {
                logger.logError("No statistics found for file '%s'", file);
            }
        }
        return selected;
    }
}
