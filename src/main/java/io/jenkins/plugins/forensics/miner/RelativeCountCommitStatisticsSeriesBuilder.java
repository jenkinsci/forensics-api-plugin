package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

import static io.jenkins.plugins.forensics.miner.RelativeCountForesnsicsSeriesBuilder.*;

/**
 * Builds one x-axis point for the series of a line chart showing the number of modified files, commits and authors in
 * the repository.
 *
 * @author Ullrich Hafner
 */
class RelativeCountCommitStatisticsSeriesBuilder extends SeriesBuilder<CommitStatisticsBuildAction> {
    @Override
    protected Map<String, Integer> computeSeries(final CommitStatisticsBuildAction current) {
        return computeRelativeCountStatistics(current.getCommitStatistics());
    }
}
