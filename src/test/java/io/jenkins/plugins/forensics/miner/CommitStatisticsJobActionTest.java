package io.jenkins.plugins.forensics.miner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.Run;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the class {@link CommitStatisticsJobAction}.
 *
 * @author Roman Boiarchuk
 */
class CommitStatisticsJobActionTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SCM_KEY = "key123";

    @Test
    void shouldCorrectlyReturnWhetherTrendIsVisible() {
        Job<?, ?> job = mock(Job.class);
        Run<?, ?> run1 = createRun(1, "run1");
        Run<?, ?> run2 = createRun(2, "run2");
        Run<?, ?> run3 = createRun(3, "run3");

        CommitStatisticsJobAction commitStatisticsJobAction = new CommitStatisticsJobAction(job, SCM_KEY);
        assertThat(commitStatisticsJobAction.isTrendVisible()).isFalse();

        when(job.getLastCompletedBuild()).thenAnswer(i -> run1);
        when(run1.getPreviousBuild()).thenAnswer(i -> run2);
        when(run2.getPreviousBuild()).thenAnswer(i -> run3);

        assertThat(commitStatisticsJobAction.isTrendVisible()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"delta", "count"})
    void shouldReturnBuildTrendModel(String chartType) {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put("buildAsDomain", true);
        configurationMap.put("numberOfBuilds", 50);
        configurationMap.put("numberOfDays", 0);
        configurationMap.put("chartType", chartType);

        String configuration = toJson(configurationMap);

        Job<?, ?> job = mock(Job.class);
        Run<?, ?> run1 = createRun(1, "run1");
        Run<?, ?> run2 = createRun(2, "run2");
        Run<?, ?> run3 = createRun(3, "run3");

        when(job.getLastCompletedBuild()).thenAnswer(i -> run1);
        when(run1.getPreviousBuild()).thenAnswer(i -> run2);
        when(run2.getPreviousBuild()).thenAnswer(i -> run3);

        CommitStatisticsJobAction commitStatisticsJobAction = new CommitStatisticsJobAction(job, SCM_KEY);
        String chartModel = commitStatisticsJobAction.getConfigurableBuildTrendModel(configuration);

        assertThat(chartModel).isNotBlank();

        assertThatJson(chartModel).node("domainAxisLabels")
                .isArray().containsExactly("run1", "run2", "run3");
        assertThatJson(chartModel).node("buildNumbers")
                .isArray().containsExactly(1, 2, 3);
    }

    private Run<?, ?> createRun(final int runNumber, final String displayName) {
        Run<?, ?> run = mock(Run.class);
        when(run.getNumber()).thenReturn(runNumber);
        when(run.getDisplayName()).thenReturn(displayName);
        when(run.getActions(CommitStatisticsBuildAction.class)).thenReturn(
                Collections.singletonList(new CommitStatisticsBuildAction(run, SCM_KEY, new CommitStatistics()))
        );
        return run;
    }

    private String toJson(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
