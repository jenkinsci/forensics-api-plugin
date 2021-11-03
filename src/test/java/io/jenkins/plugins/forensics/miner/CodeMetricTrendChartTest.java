package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link CodeMetricTrendChart}.
 *
 * @author Nikolas Paripovic
 */
class CodeMetricTrendChartTest {

    @Test
    void shouldCreate() {
        Iterable<BuildResult<ForensicsBuildAction>> buildResults = new ArrayList<>();
        ChartModelConfiguration chartModelConfiguration = createChartModelConfiguration();

        CodeMetricTrendChart codeMetricTrendChart = new CodeMetricTrendChart();
        LinesChartModel linesChartModel = codeMetricTrendChart.create(buildResults, chartModelConfiguration);

        assertThat(linesChartModel.getSeries()).isEmpty();
        assertThat(linesChartModel.getBuildNumbers()).isEmpty();
        assertThat(linesChartModel.getDomainAxisLabels()).isEmpty();
        assertThat(linesChartModel.getDomainAxisLabels()).isEmpty();
    }

    @Test
    void shouldCreateWithData() {
        Iterable<BuildResult<ForensicsBuildAction>> buildResults = createBuildResultsWithData();
        ChartModelConfiguration chartModelConfiguration = createChartModelConfiguration();

        LinesChartModel linesChartModel = codeMetricTrendChart.create(buildResults, chartModelConfiguration);

        assertThat(linesChartModel.getSeries()).hasSize(2);
        assertThat(linesChartModel.getSeries()).allSatisfy(series -> assertThat(series.getData()).hasSize(4));
        assertThat(linesChartModel.getBuildNumbers()).hasSize(4);
        assertThat(linesChartModel.getBuildNumbers()).containsExactly(1, 4, 7, 10);
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
        Build build = new Build(buildNumber);
        return new BuildResult<>(build, action);
    }

    private ChartModelConfiguration createChartModelConfiguration() {
        return new ChartModelConfiguration();
    }

}
