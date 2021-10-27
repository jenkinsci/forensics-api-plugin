package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelativeCountCommitStatisticsSeriesBuilderTest {

    @Test
    void computeSeries() {
        RelativeCountCommitStatisticsSeriesBuilder relativeCountCommitStatisticsSeriesBuilder = new RelativeCountCommitStatisticsSeriesBuilder();
        CommitStatisticsBuildAction commitStatisticsBuildActionStub = mock(CommitStatisticsBuildAction.class);
        when(commitStatisticsBuildActionStub.getCommitStatistics()).thenReturn(new CommitStatistics());

        relativeCountCommitStatisticsSeriesBuilder.computeSeries(commitStatisticsBuildActionStub);

    }
}
