package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

/**
 * Builds one x-axis point for the series of a line chart showing the number of modified files, commits and
 * authors in the repository.
 *
 * @author Ullrich Hafner
 */
class RelativeCountForensicsSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String AUTHORS_KEY = "authors";
    static final String FILES_KEY = "files";
    static final String COMMITS_KEY = "commits";

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        CommitStatistics commitStatistics;
        if (current.getTotalLinesOfCode() == 0) {
            commitStatistics = current.getResult().getLatestStatistics();
        }
        else {
            commitStatistics = current.getCommitStatistics();
        }
        return computeRelativeCountStatistics(commitStatistics);
    }

    static Map<String, Integer> computeRelativeCountStatistics(final CommitStatistics commitStatistics) {
        Map<String, Integer> series = new HashMap<>();
        series.put(AUTHORS_KEY, commitStatistics.getAuthorCount());
        series.put(FILES_KEY, commitStatistics.getFilesCount());
        series.put(COMMITS_KEY, commitStatistics.getCommitCount());
        return series;
    }
}
