package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import java.util.Arrays;

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
        var service = new MinerService();
        var logger = createLogger();

        var statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, mock(Run.class), newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages()).contains(MinerService.NO_MINER_ERROR);
    }

    @Test
    void shouldReturnEmptyStatisticsIfFilesAreEmpty() {
        var service = new MinerService();
        var logger = createLogger();

        var statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, createBuild(createAction("scm")), emptySet(), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository forensics for 0 affected files (files in repository: 0)",
                        "-> 0 affected files processed");
    }

    @Test
    void shouldFindSelectedFile() {
        var service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        var logger = createLogger();
        var statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository forensics for 1 affected files (files in repository: 1)",
                        "-> 1 affected files processed");
    }

    @Test
    void shouldFindSelectedFileForSelectedRepositories() {
        var actionWithResult = createAction("select");
        appendResult(actionWithResult);

        Run<?, ?> build = mock(Run.class);
        when(build.getActions(ForensicsBuildAction.class))
                .thenAnswer(i -> Arrays.asList(createAction("scm"), actionWithResult));

        var service = new MinerService();

        var logger = createLogger();
        var statistics = service.queryStatisticsFor(
                "select", build, newLinkedHashSet(EXISTING_FILE), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isEmpty();
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository forensics for 1 affected files (files in repository: 1)",
                        "-> 1 affected files processed");
    }

    @Test
    void shouldNotFindSelectedFile() {
        var service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        var logger = createLogger();
        var statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet("not-existing"), logger);

        assertThat(statistics).isEmpty();
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository forensics for 1 affected files (files in repository: 1)",
                        "-> 0 affected files processed");
    }

    @Test
    void shouldHandleExistingAndNotExistingFiles() {
        var service = new MinerService();

        Run<?, ?> build = configureBuildWithSingleMiningResult();

        var logger = createLogger();
        var statistics = service.queryStatisticsFor(
                NO_SCM_FILTER, build, newLinkedHashSet(EXISTING_FILE, "not-existing"), logger);

        assertThat(statistics).isNotEmpty().hasFiles(EXISTING_FILE);
        assertThat(logger.getErrorMessages()).isNotEmpty().contains("No statistics found for file 'not-existing'");
        assertThat(logger.getInfoMessages())
                .containsExactly("Extracting repository forensics for 2 affected files (files in repository: 1)",
                        "-> 1 affected files processed");
    }

    private Run<?, ?> configureBuildWithSingleMiningResult() {
        var action = createAction("scm");

        appendResult(action);

        return createBuild(action);
    }

    private void appendResult(final ForensicsBuildAction action) {
        var everything = new RepositoryStatistics();
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
