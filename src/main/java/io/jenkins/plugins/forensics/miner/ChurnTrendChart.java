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
 * Builds the Java side model for a trend chart showing the total churn for all files in the repository. The trend chart
 * contains one series that shows the total churn for all files per build. The number of builds to consider is
 * controlled by a {@link ChartModelConfiguration} instance. The created model object can be serialized to JSON (e.g.,
 * using the {@link JacksonFacade}) and can be used 1:1 as ECharts configuration object in the corresponding JS file.
 *
 * @author Giulia Del Bravo
 * @see JacksonFacade
 */
public class ChurnTrendChart {

    public LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        ChurnSeriesBuilder builder = new ChurnSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(); // TODO: should the setters be mandatory in constructor?
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());

        LineSeries churnSeries = new LineSeries(Messages.TrendChart_Churn_Legend_Label(), Palette.ORANGE.getNormal(),
                StackedMode.SEPARATE_LINES, FilledMode.LINES);
        churnSeries.addAll(dataSet.getSeries(FilesCountSeriesBuilder.TOTALS_KEY));
        model.addSeries(churnSeries);

        return model;
    }
}
