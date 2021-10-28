package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.SeriesBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RelativeCountTrendChartTest {

    private final RelativeCountTrendChart relativeCountTrendChart = new RelativeCountTrendChart();

    @Test
    void shouldCreate() {

        Iterable<BuildResult<CommitStatisticsBuildAction>> buildResult = createBuildResults(false);
        ChartModelConfiguration chartModelConfiguration = createChartModelConfiguration();
        SeriesBuilder<CommitStatisticsBuildAction> seriesBuilder = createSeriesBuilder();

        LinesChartModel linesChartModel = relativeCountTrendChart.create(buildResult, chartModelConfiguration, seriesBuilder);
        assertThat(linesChartModel.getSeries()).isEmpty();
    }

    @Test
    void shouldCreateWithData() {

        Iterable<BuildResult<CommitStatisticsBuildAction>> buildResult = createBuildResults(true);
        ChartModelConfiguration chartModelConfiguration = createChartModelConfiguration();
        SeriesBuilder<CommitStatisticsBuildAction> seriesBuilder = createSeriesBuilder();

        LinesChartModel linesChartModel = relativeCountTrendChart.create(buildResult, chartModelConfiguration, seriesBuilder);
        assertThat(linesChartModel.getSeries()).hasSize(3);
        assertThat(linesChartModel.getSeries()).allSatisfy(series -> assertThat(series.getData()).hasSize(4));
    }

    private Iterable<BuildResult<CommitStatisticsBuildAction>> createBuildResults(boolean withData) {
        List<BuildResult<CommitStatisticsBuildAction>> buildResults = new ArrayList<>();
        if(withData) {
            buildResults.add(createResult(1, 2, 3));
            buildResults.add(createResult(4, 5, 6));
            buildResults.add(createResult(7, 8, 9));
            buildResults.add(createResult(10, 11, 12));
        }
        return buildResults;
    }

    private ChartModelConfiguration createChartModelConfiguration() {
        return new ChartModelConfiguration();
    }

    private SeriesBuilder<CommitStatisticsBuildAction> createSeriesBuilder() {
        return new RelativeCountCommitStatisticsSeriesBuilder();
    }

    private BuildResult<CommitStatisticsBuildAction> createResult(final int buildNumber, final int added, final int deleted) {
        CommitStatisticsBuildAction action = mock(CommitStatisticsBuildAction.class);
        CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatistics.getAddedLines()).thenReturn(added);
        when(commitStatistics.getDeletedLines()).thenReturn(deleted);

        when(action.getCommitStatistics()).thenReturn(commitStatistics);

        Build build = new Build(buildNumber);

        return new BuildResult<>(build, action);
    }


}
