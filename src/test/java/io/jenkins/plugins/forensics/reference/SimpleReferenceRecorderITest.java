package io.jenkins.plugins.forensics.reference;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.util.FilteredLog;

import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;

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
        var job = createJob(StringUtils.EMPTY);

        Run<?, ?> build = buildSuccessfully(job);

        assertThat(findReferenceBuild(build)).isEmpty();
        assertThat(getConsoleLog(build))
                .contains("No completed build found for reference job '" + job.getDisplayName());
    }

    @Test
    void shouldUseSameJobWhenNoReferenceJobIsDefined() {
        var job = createJob(StringUtils.EMPTY);

        Run<?, ?> referenceBuild = buildSuccessfully(job);

        Run<?, ?> build = buildSuccessfully(job);

        assertThat(findReferenceBuild(build)).contains(referenceBuild);
        assertThat(getConsoleLog(build))
                .contains("No reference job configured",
                        "Falling back to current job",
                        "Found last completed build '#1' of reference job",
                        "-> Build '#1' has a result SUCCESS");
    }

    @Test
    void shouldFindReferenceBuildOfSelectedReferenceJob() {
        var reference = createFreeStyleProject();
        Run<?, ?> baseline = buildSuccessfully(reference);

        var job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
    }

    /** Uses the latest of two builds as reference build. */
    @Test
    void shouldUseLatestReferenceBuild() {
        var reference = createFreeStyleProject();
        buildSuccessfully(reference); // the first build is ignored
        Run<?, ?> baseline = buildSuccessfully(reference);

        var job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
    }

    @Test
    void shouldFindNoReferenceBuildBecauseNoBuildHasBeenCompletedYet() {
        var reference = createFreeStyleProject();

        var job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).isEmpty();
        assertThat(getConsoleLog(current)).contains("No completed build found");
    }

    @ParameterizedTest(name = "[{index}] Required result: \"{0}\"")
    @ValueSource(strings = {"SUCCESS", "UNSTABLE", ""})
    @Issue("JENKINS-72015")
    void shouldSkipFailedBuildsIfResultIsWorseThanRequired(final String requiredResult) {
        var reference = createPipeline();
        reference.setDefinition(createPipelineScript(
                """
                node {
                    brokenCommand()
                }
                """));
        buildWithResult(reference, Result.FAILURE);

        var job = createPipeline();
        String script;
        if (StringUtils.isBlank(requiredResult)) {
            script = "node {\n"
                    + discoverReferenceJob(reference.getName())
                    + " }\n";
        }
        else {
            script = "node {\n"
                    + discoverReferenceJob(reference.getName(), "requiredResult: '%s'".formatted(requiredResult))
                    + " }\n";
        }
        job.setDefinition(createPipelineScript(script));

        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).isEmpty();
        assertThat(getConsoleLog(current)).contains(
                "-> ignoring reference build '#1' or one of its predecessors since none have a result of %s or better".formatted(
                        StringUtils.defaultIfBlank(requiredResult, "UNSTABLE")));
    }

    @Test
    @Issue("JENKINS-73380")
    void shouldOverwriteReferenceBuild() {
        var reference = createPipeline();
        reference.setDefinition(createPipelineScript(
                """
                node {
                    echo 'Hello Job'
                }
                """));
        Run<?, ?> baseline = buildWithResult(reference, Result.SUCCESS);

        var job = createPipeline();
        var script = "node {\n"
                    + discoverReferenceJob(reference.getName())
                    + discoverReferenceJob(reference.getName())
                    + " }\n";
        job.setDefinition(createPipelineScript(script));

        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
        assertThat(getConsoleLog(current)).contains(
                "[-ERROR-] Replaced existing reference build, this typically indicates a misconfiguration as the reference should be constant");
    }

    @Test
    void shouldRunInDeclarativePipeline() {
        var job = createPipeline();

        job.setDefinition(createPipelineScript("pipeline {\n"
                + "    agent 'any'\n"
                + "    stages {\n"
                + "        stage ('Discover a reference build') {\n"
                + "            steps {\n"
                + discoverReferenceJob(job.getName(), "requiredBuildResult: 'SUCCESS'")
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        Run<?, ?> baseline = buildWithResult(job, Result.SUCCESS);
        Run<?, ?> second = buildSuccessfully(job);
        assertThat(findReferenceBuild(second)).contains(baseline);
    }

    private String discoverReferenceJob(final String referenceJobName, final String... arguments) {
        var joiner = new StringJoiner(", ", ", ", "").setEmptyValue("");
        Arrays.stream(arguments).forEach(joiner::add);
        return "discoverReferenceBuild(referenceJob: '%s'%s)%n".formatted(referenceJobName, joiner);
    }

    private Optional<Run<?, ?>> findReferenceBuild(final Run<?, ?> current) {
        var referenceFinder = new ReferenceFinder();
        return referenceFinder.findReference(current, new FilteredLog("LOG"));
    }

    private FreeStyleProject createJob(final String referenceJobName) {
        var job = createFreeStyleProject();
        var referenceRecorder = new SimpleReferenceRecorder();
        referenceRecorder.setReferenceJob(referenceJobName);
        job.getPublishersList().add(referenceRecorder);
        return job;
    }
}
