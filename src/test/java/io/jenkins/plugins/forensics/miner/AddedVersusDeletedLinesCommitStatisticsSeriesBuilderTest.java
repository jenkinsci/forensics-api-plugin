package io.jenkins.plugins.forensics.miner;

import static io.jenkins.plugins.forensics.miner.AddedVersusDeletedLinesForensicsSeriesBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AddedVersusDeletedLinesCommitStatisticsSeriesBuilderTest {
    @Test
    void shouldComputeSeries() {
        var builder = new AddedVersusDeletedLinesCommitStatisticsSeriesBuilder();
        var actionStub = createCommitStatisticsBuildActionStub(10, 20);

        Map<String, Integer> series = builder.computeSeries(actionStub);
        assertThat(series).containsEntry(ADDED, 10).containsEntry(DELETED, 20);
    }

    private CommitStatisticsBuildAction createCommitStatisticsBuildActionStub(
            final int addedLines, final int deletedLines) {
        CommitStatisticsBuildAction actionStub = mock(CommitStatisticsBuildAction.class);
        CommitStatistics commitStatisticsStub = mock(CommitStatistics.class);
        when(actionStub.getCommitStatistics()).thenReturn(commitStatisticsStub);
        when(commitStatisticsStub.getAddedLines()).thenReturn(addedLines);
        when(commitStatisticsStub.getDeletedLines()).thenReturn(deletedLines);
        return actionStub;
    }
}
