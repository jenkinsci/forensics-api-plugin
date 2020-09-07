package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.Palette;

public class LinesOfCodeTrendChart {

    public LinesChartModel create(final Iterable<? extends BuildResult<ForensicsBuildAction>> results,
            final ChartModelConfiguration configuration) {
        LinesOfCodeSeriesBuilder builder = new LinesOfCodeSeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel(); // TODO: should the setters be mandatory in constructor?
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());

        LineSeries series = new LineSeries(Messages.TrendChart_Files_Legend_Label(), Palette.BLUE.getNormal(),
                StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        series.addAll(dataSet.getSeries(FilesCountSeriesBuilder.TOTALS_KEY));
        model.addSeries(series);

        return model;
    }
}
