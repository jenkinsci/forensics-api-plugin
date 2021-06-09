package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.Palette;

/**
 * Builds the Java side model for a trend chart showing the number of deleted and added lines of code in a build.
 * The trend chart contains one series that shows the deleted lines and one series that shows the added lines
 * per build. The number of builds to consider is controlled by a {@link ChartModelConfiguration} instance.
 * The created model object can be serialized to JSON (e.g., using the {@link
 * JacksonFacade}) and can be used 1:1 as ECharts configuration object in the corresponding JS file.
 *
 * @author Ullrich Hafner
 * @see JacksonFacade
 */
class AddedVersusDeletedLinesTrendChart {
    LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        AddedVersusDeletedLinesSeriesBuilder builder = new AddedVersusDeletedLinesSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(dataSet);
        LineSeries newSeries = getSeries(dataSet, "Added Lines", Palette.GREEN,
                AddedVersusDeletedLinesSeriesBuilder.ADDED);
        LineSeries fixedSeries = getSeries(dataSet, "Deleted Lines", Palette.RED,
                AddedVersusDeletedLinesSeriesBuilder.DELETED);

        model.addSeries(newSeries, fixedSeries);

        return model;
    }

    private LineSeries getSeries(final LinesDataSet dataSet,
            final String name, final Palette color, final String dataSetId) {
        LineSeries newSeries = new LineSeries(name, color.getNormal(), StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        newSeries.addAll(dataSet.getSeries(dataSetId));
        return newSeries;
    }
}
