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
        FileStatistics fileStatistics = createFileStatistics();

        RepositoryStatistics statisticsOfAddAll = new RepositoryStatistics();
        statisticsOfAddAll.addAll(Collections.singleton(fileStatistics));
        verifyTotalsStatistics(statisticsOfAddAll, fileStatistics);

        RepositoryStatistics statisticsOfSingleAdd = new RepositoryStatistics();
        statisticsOfSingleAdd.add(fileStatistics);
        verifyTotalsStatistics(statisticsOfSingleAdd, fileStatistics);
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
        Commit commit = new Commit("1", "one", ONE_DAY * 9).addLines(2).deleteLines(1);
        fileStatistics.inspectCommit(commit);
        return fileStatistics;
    }
}
