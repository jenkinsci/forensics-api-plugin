package io.jenkins.plugins.forensics.miner;


import java.util.Map;

import io.jenkins.plugins.util.BuildAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelativeCountForensicsSeriesBuilderTest {

    @Test
    void computeSeries() {
        CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatistics.getAuthorCount()).thenReturn(2);
        when(commitStatistics.getFilesCount()).thenReturn(3);
        when(commitStatistics.getCommitCount()).thenReturn(4);//
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getTotalLinesOfCode()).thenReturn(2);//
        when(action.getCommitStatistics()).thenReturn(commitStatistics);
        RelativeCountForensicsSeriesBuilder builder = new RelativeCountForensicsSeriesBuilder();
        Map<String, Integer> series = builder.computeSeries(action);
        assertThat(series.get("authors")).isEqualTo(2);
        assertThat(series.get("files")).isEqualTo(3);
        assertThat(series.get("commits")).isEqualTo(4);

    }

    @Test
    void computeSeriesZeroLoc() {
        CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatistics.getAuthorCount()).thenReturn(2);
        when(commitStatistics.getFilesCount()).thenReturn(3);
        when(commitStatistics.getCommitCount()).thenReturn(4);//
        RepositoryStatistics repositoryStatistics = mock(RepositoryStatistics.class);
        when(repositoryStatistics.getLatestStatistics()).thenReturn(commitStatistics);
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getTotalLinesOfCode()).thenReturn(0);//
        when(action.getResult()).thenReturn(repositoryStatistics);
        RelativeCountForensicsSeriesBuilder builder = new RelativeCountForensicsSeriesBuilder();
        Map<String, Integer> series = builder.computeSeries(action);
        assertThat(series.get("authors")).isEqualTo(2);
        assertThat(series.get("files")).isEqualTo(3);
        assertThat(series.get("commits")).isEqualTo(4);

    }
}
