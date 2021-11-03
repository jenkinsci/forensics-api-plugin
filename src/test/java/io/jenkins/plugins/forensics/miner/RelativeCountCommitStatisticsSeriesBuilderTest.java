package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.miner.RelativeCountForensicsSeriesBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelativeCountCommitStatisticsSeriesBuilderTest {
    @Test
    void computeSeries() {
        final int commitCount = 7;
        final int authorCount = 7;
        final int filesCount = 7;

        RelativeCountCommitStatisticsSeriesBuilder relativeCountCommitStatisticsSeriesBuilder = new RelativeCountCommitStatisticsSeriesBuilder();
        CommitStatisticsBuildAction commitStatisticsBuildActionStub = getCommitStatisticsBuildActionStub(
                commitCount, authorCount, filesCount);
        Map<String, Integer> result = relativeCountCommitStatisticsSeriesBuilder.computeSeries(
                commitStatisticsBuildActionStub);

        assertThat(result)
                .containsEntry(COMMITS_KEY, commitCount)
                .containsEntry(AUTHORS_KEY, authorCount)
                .containsEntry(FILES_KEY, filesCount);
    }

    private CommitStatisticsBuildAction getCommitStatisticsBuildActionStub(final int commitCount, final int authorCount,
            final int filesCount) {
        CommitStatisticsBuildAction commitStatisticsBuildActionStub = mock(CommitStatisticsBuildAction.class);
        CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatisticsBuildActionStub.getCommitStatistics()).thenReturn(commitStatistics);
        when(commitStatistics.getCommitCount()).thenReturn(commitCount);
        when(commitStatistics.getAuthorCount()).thenReturn(authorCount);
        when(commitStatistics.getFilesCount()).thenReturn(filesCount);
        return commitStatisticsBuildActionStub;
    }
}

