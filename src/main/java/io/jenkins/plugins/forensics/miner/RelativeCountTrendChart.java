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
 * Builds the Java side model for a trend chart showing the number modified files, commits and authors in the
 * repository. The trend chart contains three series, one for each criteria.  The number of builds to consider is
 * controlled by a {@link ChartModelConfiguration} instance. The created model object can be serialized to JSON (e.g.,
 * using the {@link JacksonFacade}) and can be used 1:1 as ECharts configuration object in the corresponding JS file.
 *
 * @author Ullrich Hafner
 * @see JacksonFacade
 */
class RelativeCountTrendChart {
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
    LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        RelativeCountSeriesBuilder builder = new RelativeCountSeriesBuilder();

        LinesChartModel model = new LinesChartModel();

        LinesDataSet dataSet = builder.createDataSet(configuration, results);
        if (dataSet.getDomainAxisSize() > 0) {
            model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
            model.setBuildNumbers(dataSet.getBuildNumbers());

            LineSeries authors = getSeries(dataSet, "Authors", Palette.BLUE,
                    RelativeCountSeriesBuilder.AUTHORS_KEY);
            LineSeries commits = getSeries(dataSet, "Commits", Palette.GREEN,
                    RelativeCountSeriesBuilder.COMMITS_KEY);
            LineSeries files = getSeries(dataSet, "Modified files", Palette.ORANGE,
                    RelativeCountSeriesBuilder.FILES_KEY);

            model.addSeries(authors, commits, files);
        }

        return model;
    }

    private LineSeries getSeries(final LinesDataSet dataSet,
            final String name, final Palette color, final String dataSetId) {
        LineSeries series = new LineSeries(name, color.getNormal(), StackedMode.SEPARATE_LINES, FilledMode.LINES);
        series.addAll(dataSet.getSeries(dataSetId));
        return series;
    }

}
