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
 * Builds the Java side model for a trend chart showing the total number of lines of code for all files in the
 * repository. The trend chart contains one series that shows the total number of lines of code for all files per build.
 * The number of builds to consider is controlled by a {@link ChartModelConfiguration} instance. The created model
 * object can be serialized to JSON (e.g., using the {@link JacksonFacade}) and can be used 1:1 as ECharts configuration
 * object in the corresponding JS file.
 *
 * @author Giulia Del Bravo
 * @see JacksonFacade
 */
public class ForensicsCodeMetricTrendChart {

    public LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        ForensicsCodeMetricSeriesBuilder builder = new ForensicsCodeMetricSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(); // TODO: should the setters be mandatory in constructor?
        Palette[] colors = {Palette.BLUE, Palette.ORANGE};
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());
        int index = 0;
        for(String name : dataSet.getDataSetIds()) {
            int colorIndex = (index++)%colors.length;
            LineSeries series = new LineSeries(name, colors[colorIndex].getNormal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            series.addAll(dataSet.getSeries(name));
            model.addSeries(series);
        }

        return model;
    }
}
