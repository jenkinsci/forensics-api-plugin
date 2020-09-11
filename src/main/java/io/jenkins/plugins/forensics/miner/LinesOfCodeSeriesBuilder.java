package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

/**
 * Builds one x-axis point for the series of a line chart showing total number of lines of code for all files in the
 * repository.
 *
 * @author Giulia Del Bravo
 */
public class LinesOfCodeSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String TOTALS_KEY = "total";

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(TOTALS_KEY, current.getResult().getTotalLinesOfCode());

        return series;
    }
}
