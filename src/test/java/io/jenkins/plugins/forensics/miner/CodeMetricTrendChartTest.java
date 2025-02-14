package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CodeMetricTrendChart}.
 *
 * @author Nikolas Paripovic
 */
class CodeMetricTrendChartTest {
    @Test
    void shouldCreateEmptyChart() {
        Iterable<BuildResult<ForensicsBuildAction>> buildResults = new ArrayList<>();
        var chartModelConfiguration = createChartModelConfiguration();

        var codeMetricTrendChart = new CodeMetricTrendChart();
        var linesChartModel = codeMetricTrendChart.create(buildResults, chartModelConfiguration);

        assertThat(linesChartModel.getSeries()).isEmpty();
        assertThat(linesChartModel.getBuildNumbers()).isEmpty();
        assertThat(linesChartModel.getDomainAxisLabels()).isEmpty();
    }

    @Test
    void shouldCreateWithData() {
        Iterable<BuildResult<ForensicsBuildAction>> buildResults = createBuildResultsWithData();
        var chartModelConfiguration = createChartModelConfiguration();

        var codeMetricTrendChart = new CodeMetricTrendChart();
        var linesChartModel = codeMetricTrendChart.create(buildResults, chartModelConfiguration);

        assertThat(linesChartModel.getSeries()).hasSize(2);
        assertThat(linesChartModel.getSeries()).allSatisfy(series -> assertThat(series.getData()).hasSize(4));
        assertThat(linesChartModel.getBuildNumbers()).hasSize(4).containsExactly(1, 4, 7, 10);
    }

    private Iterable<BuildResult<ForensicsBuildAction>> createBuildResultsWithData() {
        List<BuildResult<ForensicsBuildAction>> buildResults = new ArrayList<>();
        buildResults.add(createResult(1));
        buildResults.add(createResult(4));
        buildResults.add(createResult(7));
        buildResults.add(createResult(10));
        return buildResults;
    }

    private BuildResult<ForensicsBuildAction> createResult(final int buildNumber) {
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        var build = new Build(buildNumber);
        return new BuildResult<>(build, action);
    }

    private ChartModelConfiguration createChartModelConfiguration() {
        return new ChartModelConfiguration();
    }
}
