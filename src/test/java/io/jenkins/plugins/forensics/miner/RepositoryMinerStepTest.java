package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import java.util.Collections;
import java.util.List;

import hudson.model.Run;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link RepositoryMinerStep}.
 *
 * @author Akash Manna
 */
class RepositoryMinerStepTest {
    private static final String SCM_KEY = "git [https://github.com/jenkinsci/forensics-api-plugin]";
    private static final String OTHER_SCM_KEY = "git [https://github.com/jenkinsci/git-forensics-plugin]";
    private static final String EXISTING_FILE = "src/main/java/Foo.java";

    @Test
    @Issue("JENKINS-74804")
    void shouldReturnCorrectStatisticsForEachScmKeyWithMultipleScms() {
        var statisticsForFirstScm = new RepositoryStatistics();
        statisticsForFirstScm.add(new FileStatisticsBuilder().build(EXISTING_FILE));

        ForensicsBuildAction firstScmAction = createAction(SCM_KEY, statisticsForFirstScm);
        ForensicsBuildAction secondScmAction = createAction(OTHER_SCM_KEY, new RepositoryStatistics());

        var previousBuild = mock(Run.class);
        when(previousBuild.getActions(ForensicsBuildAction.class))
                .thenReturn(List.of(firstScmAction, secondScmAction));

        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(previousBuild);

        var step = new RepositoryMinerStep();

        var resultForFirstScm = step.previousBuildStatistics(SCM_KEY, currentRun);
        assertThat(resultForFirstScm).hasFiles(EXISTING_FILE);

        var resultForSecondScm = step.previousBuildStatistics(OTHER_SCM_KEY, currentRun);
        assertThat(resultForSecondScm).isEmpty();
    }

    @Test
    @Issue("JENKINS-74804")
    void shouldReturnStatisticsForEmptyScmKeyWhenSingleRepository() {
        var statistics = new RepositoryStatistics();
        statistics.add(new FileStatisticsBuilder().build(EXISTING_FILE));

        ForensicsBuildAction action = createAction(SCM_KEY, statistics);

        var previousBuild = mock(Run.class);
        when(previousBuild.getActions(ForensicsBuildAction.class))
                .thenReturn(List.of(action));

        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(previousBuild);

        var step = new RepositoryMinerStep();

        var result = step.previousBuildStatistics("", currentRun);
        assertThat(result).hasFiles(EXISTING_FILE);
    }

    @Test
    @Issue("JENKINS-74804")
    void shouldReturnEmptyStatisticsWhenNoPreviousBuildExists() {
        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(null);

        var step = new RepositoryMinerStep();
        var result = step.previousBuildStatistics(SCM_KEY, currentRun);

        assertThat(result).isEmpty();
    }

    @Test
    @Issue("JENKINS-74804")
    void shouldReturnEmptyStatisticsWhenPreviousBuildHasNoForensicsActions() {
        var previousBuild = mock(Run.class);
        when(previousBuild.getActions(ForensicsBuildAction.class))
                .thenReturn(Collections.emptyList());
        when(previousBuild.getPreviousBuild()).thenReturn(null);

        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(previousBuild);

        var step = new RepositoryMinerStep();
        var result = step.previousBuildStatistics(SCM_KEY, currentRun);

        assertThat(result).isEmpty();
    }

    @Test
    @Issue("JENKINS-74804")
    void shouldSkipBuildsWithNoForensicsActionsAndSearchFurther() {
        var statisticsWithFiles = new RepositoryStatistics();
        statisticsWithFiles.add(new FileStatisticsBuilder().build(EXISTING_FILE));

        ForensicsBuildAction actionWithStats = createAction(SCM_KEY, statisticsWithFiles);

        var buildWithActions = mock(Run.class);
        when(buildWithActions.getActions(ForensicsBuildAction.class))
                .thenReturn(List.of(actionWithStats));
        when(buildWithActions.getPreviousBuild()).thenReturn(null);

        var buildWithNoActions = mock(Run.class);
        when(buildWithNoActions.getActions(ForensicsBuildAction.class))
                .thenReturn(Collections.emptyList());
        when(buildWithNoActions.getPreviousBuild()).thenReturn(buildWithActions);

        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(buildWithNoActions);

        var step = new RepositoryMinerStep();
        var result = step.previousBuildStatistics(SCM_KEY, currentRun);

        assertThat(result).hasFiles(EXISTING_FILE);
    }

    @Test
    @Issue("JENKINS-74804")
    void shouldReturnEmptyStatisticsWhenScmKeyDoesNotMatchAnyAction() {
        var statistics = new RepositoryStatistics();
        statistics.add(new FileStatisticsBuilder().build(EXISTING_FILE));

        ForensicsBuildAction action = createAction(SCM_KEY, statistics);

        var previousBuild = mock(Run.class);
        when(previousBuild.getActions(ForensicsBuildAction.class))
                .thenReturn(List.of(action));

        var currentRun = mock(Run.class);
        when(currentRun.getPreviousBuild()).thenReturn(previousBuild);

        var step = new RepositoryMinerStep();
        var result = step.previousBuildStatistics("svn://different-repo", currentRun);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAndSetScm() {
        var step = new RepositoryMinerStep();

        assertThat(step.getScm()).isEmpty();

        step.setScm("git");
        assertThat(step.getScm()).isEqualTo("git");
    }

    private ForensicsBuildAction createAction(final String scmKey, final RepositoryStatistics statistics) {
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getScmKey()).thenReturn(scmKey);
        when(action.getResult()).thenReturn(statistics);
        return action;
    }
}
