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
class CodeMetricSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String LOC_KEY = Messages.TrendChart_Loc_Legend_Label();
    static final String CHURN_KEY = Messages.TrendChart_Churn_Legend_Label();

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(LOC_KEY, current.getTotalLinesOfCode());
        series.put(CHURN_KEY, current.getTotalChurn());

        return series;
    }
}
