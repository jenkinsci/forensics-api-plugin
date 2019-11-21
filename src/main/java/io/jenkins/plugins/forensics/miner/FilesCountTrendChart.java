package io.jenkins.plugins.forensics.miner;

import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.LineSeries;
import io.jenkins.plugins.echarts.api.charts.LineSeries.FilledMode;
import io.jenkins.plugins.echarts.api.charts.LineSeries.StackedMode;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;
import io.jenkins.plugins.echarts.api.charts.LinesDataSet;
import io.jenkins.plugins.echarts.api.charts.Palette;

/**
 * Builds the model for a trend chart showing all issues by severity for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class FilesCountTrendChart {
    public LinesChartModel create(final Iterable<? extends BuildResult<RepositoryStatistics>> results,
            final ChartModelConfiguration configuration) {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel();
        model.setXAxisLabels(dataSet.getXAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());

        LineSeries newSeries = getSeries(dataSet, "Files totals", Palette.BLUE,
                FilesCountSeriesBuilder.TOTALS_KEY);
        model.addSeries(newSeries);

        return model;
    }

    private LineSeries getSeries(final LinesDataSet dataSet,
            final String name, final Palette color, final String dataSetId) {
        LineSeries newSeries = new LineSeries(name, color.getNormal(), StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        newSeries.addAll(dataSet.getSeries(dataSetId));
        return newSeries;
    }
}
