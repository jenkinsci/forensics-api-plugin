package io.jenkins.plugins.forensics.miner;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link RepositoryStatistics}.
 *
 * @author Ullrich Hafner
 */
class RepositoryStatisticsTest {
    private static final String NOTHING = "nothing";
    private static final String FILE = "file";

    @Test
    void shouldCreateEmptyInstance() {
        RepositoryStatistics empty = new RepositoryStatistics();

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).hasNoFiles();
        assertThat(empty).hasNoFileStatistics();
        assertThat(empty).hasNoErrorMessages();
        assertThat(empty).hasNoInfoMessages();

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.get(NOTHING));
    }

    @Test
    void shouldLogMessagesAndErrors() {
        RepositoryStatistics logger = new RepositoryStatistics();

        logger.logInfo("Hello %s", "Info");
        logger.logError("Hello %s", "Error");
        logger.logException(new IllegalArgumentException("Error"), "Hello %s", "Exception");

        assertThat(logger).hasInfoMessages("Hello Info");
        assertThat(logger).hasErrorMessages("Hello Error", "Hello Exception");

        for (int i = 0; i < 19; i++) {
            logger.logError("Hello %s %d", "Error", i);
        }
        logger.logSummary();
        assertThat(logger).hasErrorMessages("  ... skipped logging of 1 additional errors ...");
    }

    @Test
    void shouldAddStatisticsFor1File() {
        RepositoryStatistics statistics = new RepositoryStatistics();

        FileStatistics fileStatistics = new FileStatistics(FILE);

        statistics.addAll(Collections.singleton(fileStatistics));

        assertThat(statistics).isNotEmpty();
        assertThat(statistics.size()).isEqualTo(1);
        assertThat(statistics).hasFiles(FILE);
        assertThat(statistics).hasFileStatistics(fileStatistics);
        assertThat(statistics.get(FILE)).isEqualTo(fileStatistics);
    }
}
