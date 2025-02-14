package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

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
    private static final TreeString FILE_TREE_STRING = new TreeStringBuilder().intern(FILE);
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldCreateEmptyInstance() {
        var empty = new RepositoryStatistics();

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
        var repositoryStatistics = new RepositoryStatistics();
        repositoryStatistics.add(createFileStatistics());
        verifyTotalsStatistics(repositoryStatistics, createFileStatistics());
    }

    @Test
    void shouldAddAllStatisticsFor1File() {
        var repositoryStatistics = new RepositoryStatistics();
        repositoryStatistics.addAll(Set.of(createFileStatistics()));
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
        var fileStatistics = new FileStatisticsBuilder().build(FILE);
        var commit = new CommitDiffItem("1", "one", ONE_DAY * 9)
                .addLines(2)
                .deleteLines(1)
                .setNewPath(FILE_TREE_STRING);
        fileStatistics.inspectCommit(commit);
        return fileStatistics;
    }

    @Test
    void shouldAddStatisticsFor1Commit() {
        var statistics = new RepositoryStatistics();
        statistics.addAll(Collections.singletonList(createCommit()));
        assertThat(statistics).isNotEmpty()
                .hasFiles(FILE)
                .hasTotalLinesOfCode(1)
                .hasTotalChurn(5);
    }

    private CommitDiffItem createCommit() {
        return new CommitDiffItem("SHA", "author", 1)
                .deleteLines(2)
                .addLines(3)
                .setNewPath(FILE_TREE_STRING);
    }
}
