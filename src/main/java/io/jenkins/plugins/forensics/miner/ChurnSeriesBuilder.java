package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

public class ChurnSeriesBuilder extends SeriesBuilder<ForensicsBuildAction> {
    static final String TOTALS_KEY = "total";

    @Override
    protected Map<String, Integer> computeSeries(final ForensicsBuildAction current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(TOTALS_KEY, current.getResult().getTotalChurn());

        return series;
    }
}
