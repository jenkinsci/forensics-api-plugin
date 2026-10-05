package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.SeriesBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds one x-axis point for the series of a line chart showing the number of files in the repository.
 *
 * @author Ullrich Hafner
 */
class FilesCountSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String TOTALS_KEY = "total";

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(TOTALS_KEY, current.getNumberOfFiles());
        return series;
    }
}
