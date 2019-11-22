package io.jenkins.plugins.forensics.miner;

import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.JacksonFacade;
import io.jenkins.plugins.echarts.api.charts.LineSeries;
import io.jenkins.plugins.echarts.api.charts.LineSeries.FilledMode;
import io.jenkins.plugins.echarts.api.charts.LineSeries.StackedMode;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;
import io.jenkins.plugins.echarts.api.charts.LinesDataSet;
import io.jenkins.plugins.echarts.api.charts.Palette;

/**
 * Builds the Java side model for a trend chart showing the number of files in the repository. The trend chart contains
 * one series that shows the number of files per build. The number of builds to consider is controlled by a {@link
 * ChartModelConfiguration} instance. The created model object can be serialized to JSON (e.g., using the {@link
 * JacksonFacade}) and can be used 1:1 as ECharts configuration object in the corresponding JS file.
 *
 * @author Ullrich Hafner
 * @see JacksonFacade
 */
public class FilesCountTrendChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the forensics results to render - these results must be provided in descending order, i.e. the current
     *         build is the head of the list, then the previous builds, and so on
     * @param configuration
     *         the chart configuration to be used
     *
     * @return the chart model, ready to be serialized to JSON
     */
    public LinesChartModel create(final Iterable<? extends BuildResult<RepositoryStatistics>> results,
            final ChartModelConfiguration configuration) {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(); // TODO: should the setters be mandatory in constructor?
        model.setXAxisLabels(dataSet.getXAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());

        LineSeries series = new LineSeries(Messages.TrendChart_Files_Legend_Label(), Palette.BLUE.getNormal(),
                StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        series.addAll(dataSet.getSeries(FilesCountSeriesBuilder.TOTALS_KEY));
        model.addSeries(series);

        return model;
    }
}
