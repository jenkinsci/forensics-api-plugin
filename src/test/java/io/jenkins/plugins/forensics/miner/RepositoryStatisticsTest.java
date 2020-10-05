package io.jenkins.plugins.forensics.miner;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link RepositoryStatistics}.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsTest {
    private static final String NOTHING = "nothing";
    private static final String FILE = "file";
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldCreateEmptyInstance() {
        RepositoryStatistics empty = new RepositoryStatistics();

        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).isEmpty()
                .hasNoFiles()
                .hasNoFileStatistics()
                .hasLatestCommitId(StringUtils.EMPTY)
                .hasTotalLinesOfCode(0)
                .hasTotalChurn(0);

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.get(NOTHING));
    }

    @Test
    void shouldAddStatisticsFor1File() {
        RepositoryStatistics repositoryStatistics = new RepositoryStatistics();
        repositoryStatistics.add(createFileStatistics());
        verifyTotalsStatistics(repositoryStatistics, createFileStatistics());
    }

    @Test
    void shouldAddAllStatisticsFor1File() {
        RepositoryStatistics repositoryStatistics = new RepositoryStatistics();
        repositoryStatistics.addAll(Collections.singleton(createFileStatistics()));
        verifyTotalsStatistics(repositoryStatistics, createFileStatistics());
    }

    private void verifyTotalsStatistics(final RepositoryStatistics statistics, final FileStatistics fileStatistics) {
        assertThat(statistics).isNotEmpty()
                .hasFiles(FILE)
                .hasFileStatistics(fileStatistics)
                .hasTotalLinesOfCode(1)
                .hasTotalChurn(3);
        assertThat(statistics.get(FILE)).isEqualTo(fileStatistics);
    }

    private FileStatistics createFileStatistics() {
        FileStatistics fileStatistics = new FileStatisticsBuilder().build(FILE);
        Commit commit = new Commit("1", "one", ONE_DAY * 9).addLines(2).deleteLines(1).setNewPath(FILE);
        fileStatistics.inspectCommit(commit);
        return fileStatistics;
    }

    @Test
    void shouldAddStatisticsFor1Commit() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        statistics.addAll(Collections.singletonList(createCommit()));
        assertThat(statistics).isNotEmpty()
                .hasFiles(FILE)
                .hasTotalLinesOfCode(1)
                .hasTotalChurn(5);
    }

    private Commit createCommit() {
        return new Commit("SHA", "author", 1).deleteLines(2).addLines(3).setNewPath(FILE);
    }
}
