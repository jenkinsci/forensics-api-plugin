package io.jenkins.plugins.forensics.miner;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.Run;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static java.util.Collections.*;
import static org.assertj.core.util.Sets.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MinerService}.
 *
 * @author Ullrich Hafner
 */
class MinerServiceTest {
    private static final String EXISTING_FILE = "file.txt";
    private static final String NO_SCM_FILTER = StringUtils.EMPTY;

    @Test
    void shouldReturnEmptyStatisticsIfActionIsMissing() {
        MinerService service = new MinerService();
        FilteredLog logger = createLogger();

        RepositoryStatistics statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, mock(Run.class), newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages()).contains(MinerService.NO_MINER_ERROR);
    }

    @Test
    void shouldReturnEmptyStatisticsIfFilesAreEmpty() {
        MinerService service = new MinerService();
        FilteredLog logger = createLogger();

        RepositoryStatistics statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, createBuild(createAction("scm")), emptySet(), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository statistics for affected files (0 of 0)");
    }

    @Test
    void shouldFindSelectedFile() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository statistics for affected files (1 of 1)");
    }

    @Test
    void shouldFindSelectedFileForSelectedRepositories() {
        ForensicsBuildAction actionWithResult = createAction("select");
        appendResult(actionWithResult);

        Run<?, ?> build = mock(Run.class);
        when(build.getActions(ForensicsBuildAction.class))
                .thenAnswer(i -> Arrays.asList(createAction("scm"), actionWithResult));

        MinerService service = new MinerService();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(
                "select", build, newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository statistics for affected files (1 of 1)");
    }

    @Test
    void shouldNotFindSelectedFile() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet("not-existing"), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository statistics for affected files (1 of 1)");
    }

    @Test
    void shouldHandleExistingAndNotExistingFiles() {
        MinerService service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        FilteredLog logger = createLogger();
        RepositoryStatistics statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet(EXISTING_FILE, "not-existing"), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository statistics for affected files (2 of 1)");
    }

    private Run<?, ?> configureBuildWithSingleMiningResult() {
        ForensicsBuildAction action = createAction("scm");

        appendResult(action);

        return createBuild(action);
    }

    private void appendResult(final ForensicsBuildAction action) {
        RepositoryStatistics everything = new RepositoryStatistics();
        everything.add(new FileStatisticsBuilder().build(EXISTING_FILE));
        when(action.getResult()).thenReturn(everything);
    }

    private Run<?, ?> createBuild(final ForensicsBuildAction action) {
        Run<?, ?> build = mock(Run.class);
        when(build.getActions(ForensicsBuildAction.class))
                .thenAnswer(i -> singletonList(action));
        return build;
    }

    private ForensicsBuildAction createAction(final String scm) {
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getScmKey()).thenReturn(scm);
        return action;
    }

    private FilteredLog createLogger() {
        return new FilteredLog("Errors");
    }
}
