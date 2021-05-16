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
    public LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel();
        if (dataSet.getDomainAxisSize() > 0) {
            model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
            model.setBuildNumbers(dataSet.getBuildNumbers());
        }

        LineSeries series = new LineSeries(Messages.TrendChart_Files_Legend_Label(), Palette.BLUE.getNormal(),
                StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        if (dataSet.getDomainAxisSize() > 0) {
            series.addAll(dataSet.getSeries(FilesCountSeriesBuilder.TOTALS_KEY));
            model.addSeries(series);
        }
        return model;
    }
}
