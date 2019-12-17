package io.jenkins.plugins.forensics.miner;

import java.util.Collections;
import java.util.NoSuchElementException;

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

    @Test
    void shouldCreateEmptyInstance() {
        RepositoryStatistics empty = new RepositoryStatistics();

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).hasNoFiles();
        assertThat(empty).hasNoFileStatistics();

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.get(NOTHING));
    }

    @Test
    void shouldAddStatisticsFor1File() {
        RepositoryStatistics statistics = new RepositoryStatistics();

        FileStatistics fileStatistics = new FileStatisticsBuilder().build(FILE);

        statistics.addAll(Collections.singleton(fileStatistics));

        assertThat(statistics).isNotEmpty();
        assertThat(statistics.size()).isEqualTo(1);
        assertThat(statistics).hasFiles(FILE);
        assertThat(statistics).hasFileStatistics(fileStatistics);
        assertThat(statistics.get(FILE)).isEqualTo(fileStatistics);
    }
}
