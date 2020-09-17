package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link RepositoryStatisticsXmlStream}.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsXmlStreamTest extends SerializableTest<RepositoryStatistics> {
    private static final String FILE = "/path/to/file.txt";
    private static final int ONE_DAY = 60 * 60 * 24;
    private static final String ISSUE_BUILDER = "/analysis/IssueBuilder.java";

    @Test
    void shouldReadBlamesOfForensics062() {
        assertThatForensicsAreCorrect(read("forensics-0.6.2.xml"));
    }

    @Test
    void shouldReadBlamesOfForensics070() {
        assertThatForensicsAreCorrect(read("forensics-0.7.0.xml"));
    }

    private RepositoryStatistics read(final String fileName) {
        RepositoryStatisticsXmlStream repositoryStatisticsReader = new RepositoryStatisticsXmlStream();

        return repositoryStatisticsReader.read(getResourceAsFile(fileName));
    }

    private void assertThatForensicsAreCorrect(final RepositoryStatistics statistics) {
        assertThat(statistics)
                .hasOnlyFiles(ISSUE_BUILDER, "/analysis/Report.java", "/analysis/FilteredLog.java");

        FileStatistics fileStatistics = statistics.get(ISSUE_BUILDER);
        assertThat(fileStatistics).hasFileName(ISSUE_BUILDER)
                .hasCreationTime(1_506_775_701)
                .hasLastModificationTime(1_546_429_687)
                .hasNumberOfAuthors(1)
                .hasNumberOfCommits(32);
    }

    @Test
    void shouldWriteReport() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        FileStatistics fileStatistics = new FileStatisticsBuilder().build(FILE);
        fileStatistics.inspectCommit(ONE_DAY * 4, "name", 0, "1", 0, 0);
        fileStatistics.inspectCommit(ONE_DAY * 3, "another", 0, "2", 0, 0);
        fileStatistics.inspectCommit(ONE_DAY * 2, "another", 0, "3", 0, 0);
        statistics.add(fileStatistics);

        RepositoryStatisticsXmlStream stream = new RepositoryStatisticsXmlStream();
        Path path = createTempFile();
        stream.write(path, statistics);

        RepositoryStatistics restored = stream.read(path);

        assertThat(restored).hasFiles(FILE);
        FileStatistics restoredFile = restored.get(FILE);
        assertThat(restoredFile).hasNumberOfAuthors(2);
        assertThat(restoredFile).hasNumberOfCommits(3);
        assertThat(restoredFile).hasCreationTime(ONE_DAY * 2);
        assertThat(restoredFile).hasLastModificationTime(ONE_DAY * 4);
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("test", ".xml");
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    @Override
    protected RepositoryStatistics createSerializable() {
        return read("forensics-0.7.0.xml");
    }
}
