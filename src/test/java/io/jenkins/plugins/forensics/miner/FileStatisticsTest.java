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
                .hasLinesOfCode(0);

        statistics.inspectCommit(ONE_DAY * 9, "one", 0, "1", 1, 0);
        assertThat(statistics).hasNumberOfCommits(1)
                .hasCommits("1")
                .hasNumberOfAuthors(1)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 9)
                .hasLinesOfCode(1);
        assertThat(statistics.getAddedLines("1")).isOne();
        assertThat(statistics.getDeletedLines("1")).isZero();
        assertThat(statistics.getAuthor("1")).isEqualTo("one");

        statistics.inspectCommit(ONE_DAY * 8, "one", 0, "2", 2, 0);
        assertThat(statistics).hasNumberOfCommits(2)
                .hasCommits("1", "2")
                .hasNumberOfAuthors(1)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 8)
                .hasLinesOfCode(3);
        assertThat(statistics.getAddedLines("2")).isEqualTo(2);
        assertThat(statistics.getDeletedLines("2")).isZero();
        assertThat(statistics.getAuthor("2")).isEqualTo("one");

        statistics.inspectCommit(ONE_DAY * 7, "two", 0, "3", 0, 1);
        assertThat(statistics).hasNumberOfCommits(3)
                .hasCommits("1", "2", "3")
                .hasNumberOfAuthors(2)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 7)
                .hasLinesOfCode(2);
        assertThat(statistics.getAddedLines("3")).isZero();
        assertThat(statistics.getDeletedLines("3")).isOne();
        assertThat(statistics.getAuthor("3")).isEqualTo("two");

        statistics.inspectCommit(ONE_DAY * 7, "three", 0, "4", 0, 2);
        assertThat(statistics).hasNumberOfCommits(4)
                .hasCommits("1", "2", "3", "4")
                .hasNumberOfAuthors(3)
                .hasLastModificationTime(ONE_DAY * 9)
                .hasCreationTime(ONE_DAY * 7)
                .hasLinesOfCode(0);
        assertThat(statistics.getAddedLines("4")).isZero();
        assertThat(statistics.getDeletedLines("4")).isEqualTo(2);
        assertThat(statistics.getAuthor("4")).isEqualTo("three");
    }

    @Test
    void shouldConvertWindowsName() {
        assertThat(createStatistics("C:\\path\\to\\file.txt")).hasFileName("C:/path/to/file.txt");
        assertThat(createStatistics("C:\\path\\to/file.txt")).hasFileName("C:/path/to/file.txt");
        assertThat(createStatistics("/path/to/file.txt")).hasFileName("/path/to/file.txt");
    }

    @Test
    void shouldIgnoreMissingCommits() {
        FileStatistics statistics = createStatistics(FILE);

        assertThat(statistics.getAddedLines("does-not-exist")).isZero();
        assertThat(statistics.getDeletedLines("does-not-exist")).isZero();
        assertThat(statistics.getLinesOfCode()).isZero();
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
