package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

/**
 * Builds one x-axis point for the series of a line chart showing the added and deleted lines per build.
 *
 * @author Ullrich Hafner
 */
class AddedVersusDeletedLinesCommitStatisticsSeriesBuilder extends SeriesBuilder<CommitStatisticsBuildAction> {
    static final String ADDED = "added";
    static final String DELETED = "deleted";

    @Override
    protected Map<String, Integer> computeSeries(final CommitStatisticsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        CommitStatistics commitStatistics = current.getCommitStatistics();
        series.put(ADDED, commitStatistics.getAddedLines());
        series.put(DELETED, commitStatistics.getDeletedLines());
        return series;
    }
}
