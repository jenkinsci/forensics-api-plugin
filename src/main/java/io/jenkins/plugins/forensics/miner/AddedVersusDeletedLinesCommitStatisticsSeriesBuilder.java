package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

import static io.jenkins.plugins.forensics.miner.AddedVersusDeletedLinesForensicsSeriesBuilder.*;

/**
 * Builds one x-axis point for the series of a line chart showing the added and deleted lines per build.
 *
 * @author Ullrich Hafner
 */
class AddedVersusDeletedLinesCommitStatisticsSeriesBuilder extends SeriesBuilder<CommitStatisticsBuildAction> {
    @Override
    protected Map<String, Integer> computeSeries(final CommitStatisticsBuildAction current) {
        return computeAddedVsDeletedSeries(current.getCommitStatistics());
    }
}
