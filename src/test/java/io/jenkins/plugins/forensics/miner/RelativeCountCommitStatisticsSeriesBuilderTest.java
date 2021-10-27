package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelativeCountCommitStatisticsSeriesBuilderTest {

    @Test
    void computeSeries() {
        RelativeCountCommitStatisticsSeriesBuilder relativeCountCommitStatisticsSeriesBuilder = new RelativeCountCommitStatisticsSeriesBuilder();
        CommitStatisticsBuildAction commitStatisticsBuildActionStub = mock(CommitStatisticsBuildAction.class);
        CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatisticsBuildActionStub.getCommitStatistics()).thenReturn(commitStatistics);
        when(commitStatistics.getCommitCount()).thenReturn(7);
        when(commitStatistics.getAuthorCount()).thenReturn(11);
        when(commitStatistics.getFilesCount()).thenReturn(17);

        Map<String, Integer> result = relativeCountCommitStatisticsSeriesBuilder.computeSeries(
                commitStatisticsBuildActionStub);

        assertThat(result.get("commits")).isEqualTo(7);
        assertThat(result.get("authors")).isEqualTo(11);
        assertThat(result.get("files")).isEqualTo(17);
    }
}
