package io.jenkins.plugins.forensics.reference;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SimpleReferenceRecorder}.
 *
 * @author Ullrich Hafner
 */
class SimpleReferenceRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    /** Ensures that an error is shown if the publisher has been registered with an invalid configuration. */
    @Test
    void shouldReportErrorBecauseReferenceJobIsUndefined() {
        FreeStyleProject job = createJob(StringUtils.EMPTY);

        Run<?, ?> build = buildSuccessfully(job);

        assertThat(findReferenceBuild(build)).isEmpty();
        assertThat(getConsoleLog(build))
                .contains("You need to define a valid reference job using the 'referenceJob' property");
    }

    /** Finds the reference build in the selected job. */
    @Test
    void shouldFindReferenceBuild() {
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
        buildSuccessfully(reference); // first build is ignored
        Run<?, ?> baseline = buildSuccessfully(reference);

        FreeStyleProject job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).contains(baseline);
    }

    /** Finds the reference build in the selected job. */
    @Test
    void shouldFindNoReferenceBuild() {
        FreeStyleProject reference = createFreeStyleProject();

        FreeStyleProject job = createJob(reference.getName());
        Run<?, ?> current = buildSuccessfully(job);

        assertThat(findReferenceBuild(current)).isEmpty();
        assertThat(getConsoleLog(current)).contains("No completed build found");
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
