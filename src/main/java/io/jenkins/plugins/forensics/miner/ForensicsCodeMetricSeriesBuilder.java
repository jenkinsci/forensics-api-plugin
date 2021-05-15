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
public class ForensicsCodeMetricSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String LOC_KEY = Messages.TrendChart_Loc_Legend_Label();
    static final String CHURN_KEY = Messages.TrendChart_Churn_Legend_Label();

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        int totalChurn = current.getTotalChurn();
        if (totalChurn == 0) { // results are from forensics-api < 1.1.0
            series.put(LOC_KEY, current.getResult().getTotalLinesOfCode());
            series.put(CHURN_KEY, current.getResult().getTotalChurn());
        }
        else {
            series.put(LOC_KEY, current.getTotalLinesOfCode());
            series.put(CHURN_KEY, totalChurn);
        }

        return series;
    }
}
