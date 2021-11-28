package io.jenkins.plugins.forensics.miner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.Run;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        Job job = mock(Job.class);
        Run run1 = mockRun(1, "run1");
        Run run2 = mockRun(2, "run2");
        Run run3 = mockRun(3, "run3");

        CommitStatisticsJobAction commitStatisticsJobAction = new CommitStatisticsJobAction(job, SCM_KEY);
        assertThat(commitStatisticsJobAction.isTrendVisible()).isFalse();

        when(job.getLastCompletedBuild()).thenReturn(run1);
        when(run1.getPreviousBuild()).thenReturn(run2);
        when(run2.getPreviousBuild()).thenReturn(run3);

        assertThat(commitStatisticsJobAction.isTrendVisible()).isTrue();
    }

    @Test
    void shouldReturnBuildTrendModel() {
        HashMap<String, Object> configurationMap = new HashMap<>();
        configurationMap.put("buildAsDomain", true);
        configurationMap.put("numberOfBuilds", 50);
        configurationMap.put("numberOfDays", 0);
        configurationMap.put("chartType", "delta");

        String configuration = toJson(configurationMap);

        Job job = mock(Job.class);
        Run run1 = mockRun(1, "run1");
        Run run2 = mockRun(2, "run2");
        Run run3 = mockRun(3, "run3");

        when(job.getLastCompletedBuild()).thenReturn(run1);
        when(run1.getPreviousBuild()).thenReturn(run2);
        when(run2.getPreviousBuild()).thenReturn(run3);

        CommitStatisticsJobAction commitStatisticsJobAction = new CommitStatisticsJobAction(job, SCM_KEY);
        String resultJson = commitStatisticsJobAction.getConfigurableBuildTrendModel(configuration);

        assertThat(resultJson).isNotBlank();
        Map<String, Object> chartModel = fromJson(resultJson);

        assertThat(chartModel.get("domainAxisLabels"))
                .asList().containsExactly("run1", "run2", "run3");
        assertThat(chartModel.get("buildNumbers"))
                .asList().containsExactly(1, 2, 3);
    }

    private Run mockRun(final int runNumber, final String displayName) {
        Run run = mock(Run.class);
        when(run.getNumber()).thenReturn(runNumber);
        when(run.getDisplayName()).thenReturn(displayName);
        when(run.getActions(CommitStatisticsBuildAction.class)).thenReturn(
                Collections.singletonList(new CommitStatisticsBuildAction(run, SCM_KEY, new CommitStatistics()))
        );
        return run;
    }

    private Map<String, Object> fromJson(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String toJson(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
