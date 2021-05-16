package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

/**
 * Builds the model for a trend chart showing the relationship between added and deleted lines for a given number of
 * builds.
 *
 * @author Ullrich Hafner
 */
public class AddedVersusDeletedLinesSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String ADDED = "added";
    static final String DELETED = "deleted";

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        CommitStatistics commitStatistics;
        if (current.getTotalLinesOfCode() == 0) {
            commitStatistics = current.getResult().getLatestStatistics();
        }
        else {
            commitStatistics = current.getCommitStatistics();
        }
        series.put(ADDED, commitStatistics.getAddedLines());
        series.put(DELETED, commitStatistics.getDeletedLines());
        return series;
    }
}
