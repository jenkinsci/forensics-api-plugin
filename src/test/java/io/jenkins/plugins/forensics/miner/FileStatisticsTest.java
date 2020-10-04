package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link FileStatistics}.
 *
 * @author Ullrich Hafner
 */
class FileStatisticsTest extends SerializableTest<FileStatistics> {
    private static final String FILE = "file";
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldCreateFileStatistics() {
        FileStatistics statistics = new FileStatisticsBuilder().build(FILE);
        assertThat(statistics).hasFileName(FILE)
                .hasNumberOfCommits(0)
                .hasNumberOfAuthors(0)
                .hasLastModificationTime(0)
                .hasCreationTime(0)
                .hasLinesOfCode(0)
                .hasAbsoluteChurn(0);

        Commit first = new Commit("1", "one", ONE_DAY * 9).addLines(1);
        statistics.inspectCommit(first);
        assertThat(statistics).hasNumberOfCommits(1)
                .hasCommits(first)
                .hasNumberOfAuthors(1)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 9)
                .hasLinesOfCode(1)
                .hasAbsoluteChurn(1);

        Commit second = new Commit("2", "one", ONE_DAY * 8).addLines(2);
        statistics.inspectCommit(second);
        assertThat(statistics).hasNumberOfCommits(2)
                .hasCommits(first, second)
                .hasNumberOfAuthors(1)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 8)
                .hasLinesOfCode(3)
                .hasAbsoluteChurn(3);

        Commit third = new Commit("3", "two", ONE_DAY * 7).deleteLines(1);
        statistics.inspectCommit(third);
        assertThat(statistics).hasNumberOfCommits(3)
                .hasCommits(first, second, third)
                .hasNumberOfAuthors(2)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 7)
                .hasLinesOfCode(2)
                .hasAbsoluteChurn(4);

        Commit fourth = new Commit("4", "three", ONE_DAY * 6).deleteLines(2);
        statistics.inspectCommit(fourth);
        assertThat(statistics).hasNumberOfCommits(4)
                .hasCommits(first, second, third, fourth)
                .hasNumberOfAuthors(3)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 6)
                .hasLinesOfCode(0).hasAbsoluteChurn(6);
    }

    @Test
    void shouldConvertWindowsName() {
        assertThat(createStatistics("C:\\path\\to\\file.txt")).hasFileName("C:/path/to/file.txt");
        assertThat(createStatistics("C:\\path\\to/file.txt")).hasFileName("C:/path/to/file.txt");
        assertThat(createStatistics("/path/to/file.txt")).hasFileName("/path/to/file.txt");
    }

    @Override
    protected FileStatistics createSerializable() {
        FileStatistics statistics = createStatistics(FILE);

        statistics.inspectCommit(ONE_DAY * 9, "one");

        return statistics;
    }

    private FileStatistics createStatistics(final String fileName) {
        return new FileStatisticsBuilder().build(fileName);
    }
}
