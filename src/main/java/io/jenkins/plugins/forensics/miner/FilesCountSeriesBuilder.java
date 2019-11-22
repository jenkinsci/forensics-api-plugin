package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.SeriesBuilder;

/**
 * Builds one x-axis point for the series of a line chart showing the number of files in the repository.
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
