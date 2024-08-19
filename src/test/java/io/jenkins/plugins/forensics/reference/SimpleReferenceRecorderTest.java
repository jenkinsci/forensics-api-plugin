package io.jenkins.plugins.forensics.reference;

import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import hudson.model.BuildableItem;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder.SimpleReferenceRecorderDescriptor;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleReferenceRecorderTest {
    @Test
    void shouldFillModel() {
        var jenkins = mock(JenkinsFacade.class);
        var job = mock(BuildableItem.class);

        var descriptor = new SimpleReferenceRecorderDescriptor(jenkins);

        assertThat(descriptor.doFillReferenceJobItems(job)).isEmpty();
        assertThat(descriptor.doFillRequiredResultItems(job)).isEmpty();

        var jobs = Set.of("one", "two");
        when(jenkins.getAllJobNames()).thenReturn(jobs);
        when(jenkins.hasPermission(Item.CONFIGURE, job)).thenReturn(true);

        assertThat(descriptor.doFillReferenceJobItems(job))
                .containsExactlyInAnyOrderElementsOf(jobs);
        assertThat(descriptor.doFillRequiredResultItems(job))
                .extracting("value")
                .containsExactlyInAnyOrder("FAILURE", "SUCCESS", "UNSTABLE");
    }

    @Test
    void shouldValidateJob() {
        var jenkins = mock(JenkinsFacade.class);
        var job = mock(BuildableItem.class);
        var model = mock(ReferenceJobModelValidation.class);

        when(jenkins.hasPermission(Item.CONFIGURE, job)).thenReturn(true);
        when(model.validateJob("one")).thenReturn(FormValidation.ok());
        when(model.validateJob("two")).thenReturn(FormValidation.error("error"));

        var descriptor = new SimpleReferenceRecorderDescriptor(jenkins, model);

        assertThat(descriptor.doCheckReferenceJob(job, "one").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckReferenceJob(job, "two").kind).isEqualTo(Kind.ERROR);
    }

    @Test
    void shouldConsiderRunningBuilds() {
        var recorder = new SimpleReferenceRecorder();

        assertThat(recorder)
                .hasRequiredResult(Result.UNSTABLE)
                .isNotConsiderRunningBuild();

        var run = mock(Run.class);
        var job = mock(FreeStyleProject.class);
        when(job.getDisplayName()).thenReturn("reference");

        when(run.getParent()).thenReturn(job);
        when(run.getResult()).thenReturn(Result.SUCCESS);

        var log = createLog();

        var noReferenceBuild = recorder.findReferenceBuild(run, log);

        assertThat(log.getInfoMessages()).contains(
                "No reference job configured",
                "Falling back to current job 'reference'",
                "No completed build found for reference job 'reference'");

        recorder.setConsiderRunningBuild(true);

        log = createLog();
        recorder.findReferenceBuild(run, log);

        assertThat(log.getInfoMessages()).contains(
                "No reference job configured",
                "Falling back to current job 'reference'",
                "No build found for reference job 'reference'");

        assertThat(noReferenceBuild).hasReferenceBuildId("-");

        recorder.setConsiderRunningBuild(false);

        FreeStyleBuild reference = mock(FreeStyleBuild.class);
        when(reference.getResult()).thenReturn(Result.SUCCESS);
        when(reference.getDisplayName()).thenReturn("reference-build");
        when(reference.getExternalizableId()).thenReturn("reference-build-id");

        when(job.getLastCompletedBuild()).thenReturn(reference);

        log = createLog();
        var referenceBuild = recorder.findReferenceBuild(run, log);

        assertThat(log.getInfoMessages()).contains(
                "Found last completed build 'reference-build' of reference job 'reference'",
                "-> Build 'reference-build' has a result SUCCESS");
        assertThat(referenceBuild).hasReferenceBuildId("reference-build-id");

        when(job.getLastCompletedBuild()).thenReturn(null);
        when(job.getLastBuild()).thenReturn(reference);

        log = createLog();

        var noCompletedBuild = recorder.findReferenceBuild(run, log);

        assertThat(log.getInfoMessages()).contains(
                "No reference job configured",
                "Falling back to current job 'reference'",
                "No completed build found for reference job 'reference'");

        assertThat(noCompletedBuild).hasReferenceBuildId("-");

        log = createLog();

        recorder.setConsiderRunningBuild(true);
        var runningBuild = recorder.findReferenceBuild(run, log);

        assertThat(log.getInfoMessages()).contains(
                "Found last completed build 'reference-build' of reference job 'reference'",
                "-> Build 'reference-build' has a result SUCCESS");
        assertThat(runningBuild).hasReferenceBuildId("reference-build-id");
    }

    private FilteredLog createLog() {
        return new FilteredLog("test");
    }
}
