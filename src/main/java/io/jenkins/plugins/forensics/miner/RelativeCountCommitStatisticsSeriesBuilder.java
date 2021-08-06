package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

/**
 * Builds one x-axis point for the series of a line chart showing the number of modified files, commits and authors in
 * the repository.
 *
 * @author Ullrich Hafner
 */
class RelativeCountCommitStatisticsSeriesBuilder extends SeriesBuilder<CommitStatisticsBuildAction> {
    static final String AUTHORS_KEY = "authors";
    static final String FILES_KEY = "files";
    static final String COMMITS_KEY = "commits";

    @Override
    protected Map<String, Integer> computeSeries(final CommitStatisticsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        CommitStatistics commitStatistics = current.getCommitStatistics();
        series.put(AUTHORS_KEY, commitStatistics.getAuthorCount());
        series.put(FILES_KEY, commitStatistics.getFilesCount());
        series.put(COMMITS_KEY, commitStatistics.getCommitCount());
        return series;
    }
}
