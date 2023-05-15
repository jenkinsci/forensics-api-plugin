package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LinesChartModel;

import io.jenkins.plugins.echarts.JenkinsPalette;

import static io.jenkins.plugins.forensics.miner.ResultStubs.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link FilesCountTrendChart}.
 *
 * @author Ullrich Hafner
 */
class FilesCountTrendChartTest {
    @Test
    void shouldCreateLinesChartModel() {
        FilesCountTrendChart chart = new FilesCountTrendChart();

        List<BuildResult<ForensicsBuildAction>> results = new ArrayList<>();
        results.add(createResult(2, 20));
        results.add(createResult(1, 10));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), JenkinsPalette.BLUE, Messages.TrendChart_Files_Legend_Label(), 10, 20);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(model).node("series")
                .isArray().hasSize(1);
        assertThatJson(model).node("series[0].data")
                .isArray().hasSize(2).containsExactly(10, 20);
    }

    private void verifySeries(final LineSeries series, final JenkinsPalette normalColor, final String newVersusFixedSeriesBuilderName, final int... values) {
        assertThatJson(series).node("itemStyle").node("color").isEqualTo(normalColor.normal());
        assertThatJson(series).node("name").isEqualTo(newVersusFixedSeriesBuilderName);
        for (int value : values) {
            assertThatJson(series).node("data").isArray().hasSize(values.length).contains(value);
        }
    }
}
