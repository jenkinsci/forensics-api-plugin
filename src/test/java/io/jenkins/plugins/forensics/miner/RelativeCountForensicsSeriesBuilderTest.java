package io.jenkins.plugins.forensics.miner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeCountForensicsSeriesBuilderTest {

    private static final java.lang.String AUTHORS = "authors";
    private static final String FILES = "files";
    private static final String COMMITS = "commits";

    @Test
    void shouldComputeRelativeCountStatistics() {
		CommitStatistics commitStatistics = mock(CommitStatistics.class);
        when(commitStatistics.getAuthorCount()).thenReturn(5);
        when(commitStatistics.getFilesCount()).thenReturn(7);
        when(commitStatistics.getCommitCount()).thenReturn(3);
        
        Map<String, Integer> countStatistics = RelativeCountForensicsSeriesBuilder.computeRelativeCountStatistics(commitStatistics);
        
        assertThat(countStatistics).hasSize(3);
        assertThat(countStatistics.get(AUTHORS)).isEqualTo(5);
        assertThat(countStatistics.get(FILES)).isEqualTo(7);
        assertThat(countStatistics.get(COMMITS)).isEqualTo(3);
	}

}
