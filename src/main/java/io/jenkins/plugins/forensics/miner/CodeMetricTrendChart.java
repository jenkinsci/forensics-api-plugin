package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;

import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the Java side model for a trend chart showing the total number of lines of code and churn for all files in the
 * repository. The trend chart contains one series that shows the total number of lines of code for all files per build
 * and a series that shows the total churn of all files per build. The number of builds to consider is controlled by a
 * {@link ChartModelConfiguration} instance. The created model object can be serialized to JSON (e.g., using the {@link
 * JacksonFacade}) and can be used 1:1 as ECharts configuration object in the corresponding JS file.
 *
 * @author Giulia Del Bravo
 * @see JacksonFacade
 */
class CodeMetricTrendChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the forensics results to render - these results must be provided in descending order, i.e. the current *
     *         build is the head of the list, then the previous builds, and so on
     * @param configuration
     *         the chart configuration to be used
     *
     * @return the chart model, ready to be serialized to JSON
     */
    LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        var builder = new CodeMetricSeriesBuilder();
        var dataSet = builder.createDataSet(configuration, results);

        var model = new LinesChartModel(dataSet);
        JenkinsPalette[] colors = {JenkinsPalette.BLUE, JenkinsPalette.ORANGE};
        int index = 0;
        for (String name : dataSet.getDataSetIds()) {
            int colorIndex = index % colors.length;
            var series = new LineSeries(name, colors[colorIndex].normal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            series.addAll(dataSet.getSeries(name));
            model.addSeries(series);

            index++;
        }

        return model;
    }
}
