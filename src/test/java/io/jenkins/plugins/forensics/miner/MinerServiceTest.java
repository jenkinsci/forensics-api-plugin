package io.jenkins.plugins.forensics.miner;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.assertj.core.util.Sets.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MinerService}.
 *
 * @author Ullrich Hafner
 */
class MinerServiceTest {
    private static final String EXISTING_FILE = "file.txt";

    @Test
    void shouldReturnEmptyStatisticsIfActionIsMissing() {
        MinerService service = new MinerService();
        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(mock(Run.class),
                newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages()).contains(MinerService.NO_MINER_CONFIGURED_ERROR);
    }

    @Test
    void shouldReturnEmptyStatisticsIfFilesAreEmpty() {
        MinerService service = new MinerService();
        Run<?, ?> build = mock(Run.class);
        when(build.getAction(ForensicsBuildAction.class)).thenReturn(mock(ForensicsBuildAction.class));

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(build,
                Collections.emptySet(), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages()).isEmpty();
    }

    @Test
    void shouldFindSelectedFile() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(build,
                newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages()).isEmpty();
    }

    @Test
    void shouldNotFindSelectedFile() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(build,
                newLinkedHashSet("not-existing"), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages()).isEmpty();
    }

    @Test
    void shouldHandleExistingAndNotExistingFiles() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(build,
                newLinkedHashSet(EXISTING_FILE, "not-existing"), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages()).isEmpty();
    }

    private Run<?, ?> configureBuildWithSingleMiningResult() {
        Run<?, ?> build = mock(Run.class);
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(build.getAction(ForensicsBuildAction.class)).thenReturn(action);

        RepositoryStatistics everything = new RepositoryStatistics();
        everything.add(new FileStatisticsBuilder().build(EXISTING_FILE));

        when(action.getResult()).thenReturn(everything);
        return build;
    }

    private FilteredLog createLogger() {
        return new FilteredLog("Errors");
    }
}
