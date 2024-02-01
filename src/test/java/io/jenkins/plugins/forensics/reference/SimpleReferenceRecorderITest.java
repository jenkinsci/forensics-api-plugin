package io.jenkins.plugins.forensics.reference;

import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.util.FilteredLog;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SimpleReferenceRecorder}.
 *
 * @author Ullrich Hafner
 */
class SimpleReferenceRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    void shouldReportErrorBecauseReferenceJobIsUndefinedAndNoBuildsInSameSob() {
        FreeStyleProject job = createJob(StringUtils.EMPTY);

        Run<?, ?> build = buildSuccessfully(job);

        assertThat(findReferenceBuild(build)).isEmpty();
        assertThat(getConsoleLog(build))
                .contains("No completed build found for reference job '" + job.getDisplayName());
    }

    @Test
    void shouldUseSameJobWhenNoReferenceJobIsDefined() {
        FreeStyleProject job = createJob(StringUtils.EMPTY);

        Run<?, ?> referenceBuild = buildSuccessfully(job);

        Run<?, ?> build = buildSuccessfully(job);

        assertThat(findReferenceBuild(build)).contains(referenceBuild);
        assertThat(getConsoleLog(build))
                .contains("No reference job configured",
                        "Falling back to current job",
                        "Found reference build '#1' of reference job '" + job.getDisplayName());
    }

    @Test
    void shouldFindReferenceBuildOfSelectedReferenceJob() {
        FreeStyleProject reference = createFreeStyleProject();
        Run<?, ?> baseline = buildSuccessfully(reference);

        FreeStyleProject job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
    }

    /** Uses the latest of two builds as reference build. */
    @Test
    void shouldUseLatestReferenceBuild() {
        FreeStyleProject reference = createFreeStyleProject();
        buildSuccessfully(reference); // the first build is ignored
        Run<?, ?> baseline = buildSuccessfully(reference);

        FreeStyleProject job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
    }

    @Test
    void shouldFindNoReferenceBuildBecauseNoBuildHasBeenCompletedYet() {
        FreeStyleProject reference = createFreeStyleProject();

        FreeStyleProject job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).isEmpty();
        assertThat(getConsoleLog(current)).contains("No completed build found");
    }

    @Test
    void shouldUseFailedBuildsByDefault() {
        WorkflowJob reference = createPipeline();
        reference.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "brokenCommand()\n"
                        + " }\n", true));
        Run<?, ?> referenceBuild = buildWithResult(reference, Result.FAILURE);

        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + discoverReferenceJob(reference.getName())
                        + " }\n", true));
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(referenceBuild);
        assertThat(getConsoleLog(current))
                .contains("Found reference build '#1' of reference job '" + reference.getDisplayName());
    }

    @ParameterizedTest(name = "[{index}] Required result: {0}")
    @ValueSource(strings = {"SUCCESS", "UNSTABLE"})
    @Issue("JENKINS-72015")
    void shouldSkipFailedBuildsIfResultIsWorseThanRequired(final String requiredResult) {
        WorkflowJob reference = createPipeline();
        reference.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + "brokenCommand()\n"
                        + " }\n", true));
        buildWithResult(reference, Result.FAILURE);

        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                        + discoverReferenceJob(reference.getName(), String.format("requiredResult: '%s'", requiredResult))
                        + " }\n", true));

        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).isEmpty();
        assertThat(getConsoleLog(current)).contains(
                String.format("-> ignoring reference build '#1' since it has a result of FAILURE, "
                        + "but required is %s or better", requiredResult));
    }

    private String discoverReferenceJob(final String referenceJobName, final String... arguments) {
        var joiner = new StringJoiner(", ", ", ", "").setEmptyValue("");
        Arrays.stream(arguments).forEach(joiner::add);
        return String.format("discoverReferenceBuild(referenceJob: '%s'%s)%n", referenceJobName, joiner);
    }

    private Optional<Run<?, ?>> findReferenceBuild(final Run<?, ?> current) {
        ReferenceFinder referenceFinder = new ReferenceFinder();
        return referenceFinder.findReference(current, new FilteredLog("LOG"));
    }

    private FreeStyleProject createJob(final String referenceJobName) {
        FreeStyleProject job = createFreeStyleProject();
        SimpleReferenceRecorder referenceRecorder = new SimpleReferenceRecorder();
        referenceRecorder.setReferenceJob(referenceJobName);
        job.getPublishersList().add(referenceRecorder);
        return job;
    }
}
