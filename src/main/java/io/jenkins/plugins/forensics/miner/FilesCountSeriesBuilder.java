package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.SeriesBuilder;

/**
 * Builds the series for a stacked line chart showing all issues by severity.
 *
 * @author Ullrich Hafner
 */
public class FilesCountSeriesBuilder extends SeriesBuilder<RepositoryStatistics> {
    static final String TOTALS_KEY = "total";

    @Override
    protected Map<String, Integer> computeSeries(final BuildResult<RepositoryStatistics> current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(TOTALS_KEY, current.getResult().size());
        return series;
    }
}
