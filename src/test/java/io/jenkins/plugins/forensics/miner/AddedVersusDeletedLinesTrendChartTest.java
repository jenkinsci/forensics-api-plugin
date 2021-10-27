package io.jenkins.plugins.forensics.miner;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.SeriesBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddedVersusDeletedLinesTrendChartTest {
    @Test
    void shouldCreate() {
        AddedVersusDeletedLinesTrendChart builder = new AddedVersusDeletedLinesTrendChart();

        Iterable<BuildResult<CommitStatisticsBuildAction>> buildResultsStub = createBuildResultsStub();
        ChartModelConfiguration chartModelConfigurationStub = createConfiguration();

        SeriesBuilder seriesBuilderStub = createSeriesBuilderStub();

        LinesChartModel lineChartModel = builder.create(buildResultsStub, chartModelConfigurationStub, seriesBuilderStub);

        assertThat(lineChartModel)
                .satisfies(e -> {
                    assertThat(e.getSeries()).isNotEmpty();
                });
    }

    private SeriesBuilder createSeriesBuilderStub() {
        SeriesBuilder seriesBuilderStub = mock(SeriesBuilder.class);
        LinesDataSet linesDataSet = mock(LinesDataSet.class);

        ChartModelConfiguration configurationStub = createConfiguration();
        Iterable<BuildResult<CommitStatisticsBuildAction>> buildResultsStub = createBuildResultsStub();

        when(seriesBuilderStub.createDataSet(configurationStub, buildResultsStub))
                .thenReturn(linesDataSet);

        return seriesBuilderStub;
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }

    private Iterable<BuildResult<CommitStatisticsBuildAction>> createBuildResultsStub() {
        return new Iterable<BuildResult<CommitStatisticsBuildAction>>() {
            @Override
            public Iterator<BuildResult<CommitStatisticsBuildAction>> iterator() {
                return null;
            }
        };
    }
}

    //class AddedVersusDeletedLinesCommitStatisticsSeriesBuilderTest {
//    @Test
//    void shouldComputeSeries() {
//        AddedVersusDeletedLinesCommitStatisticsSeriesBuilder builder = new AddedVersusDeletedLinesCommitStatisticsSeriesBuilder();
//        CommitStatisticsBuildAction actionStub = createCommitStatisticsBuildActionStub(10, 20);
//
//        Map<String, Integer> series = builder.computeSeries(actionStub);
//        assertThat(series)
//                .containsEntry(ADDED, 10)
//                .containsEntry(DELETED, 20);
//    }
//
//    private CommitStatisticsBuildAction createCommitStatisticsBuildActionStub(final int addedLines,
//            final int deletedLines) {
//        CommitStatisticsBuildAction actionStub = mock(CommitStatisticsBuildAction.class);
//        CommitStatistics commitStatisticsStub = mock(CommitStatistics.class);
//        when(actionStub.getCommitStatistics()).thenReturn(commitStatisticsStub);
//        when(commitStatisticsStub.getAddedLines()).thenReturn(addedLines);
//        when(commitStatisticsStub.getDeletedLines()).thenReturn(deletedLines);
//        return actionStub;
//    }
//}
