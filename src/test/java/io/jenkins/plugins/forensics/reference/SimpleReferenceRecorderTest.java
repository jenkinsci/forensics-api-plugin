package io.jenkins.plugins.forensics.reference;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.util.List;
import java.util.Set;

import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ModelObject;
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
    void shouldNotRequireWorkspace() {
        var recorder = new SimpleReferenceRecorder();

        assertThat(recorder.requiresWorkspace()).isFalse();
    }

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

    @Test
    @Issue("JENKINS-72825")
    void shouldNotFilterByActionsByDefault() {
        var recorder = new SimpleReferenceRecorder();

        assertThat(recorder)
                .hasRequiredAction(StringUtils.EMPTY)
                .hasRequiredActionId(StringUtils.EMPTY);
        assertThat(recorder.createActionFilter().isEnabled()).isFalse();
        assertThat(recorder.createActionFilter().accepts(mock(Run.class))).isTrue();
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldStripWhitespaceFromActionFilter() {
        var recorder = new SimpleReferenceRecorder();

        recorder.setRequiredAction("  CoverageBuildAction  ");
        recorder.setRequiredActionId("  coverage  ");

        assertThat(recorder)
                .hasRequiredAction("CoverageBuildAction")
                .hasRequiredActionId("coverage");
        assertThat(recorder.createActionFilter().isEnabled()).isTrue();
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldSkipBuildsThatDoNotProvideTheRequiredActionType() {
        var withoutReport = createBuild("no-report", Result.SUCCESS);
        var withReport = createBuild("report", Result.SUCCESS, new CoverageReportAction("coverage"));

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredAction(CoverageReportAction.class.getName());

        var log = createLog();
        var referenceBuild = recorder.findReferenceBuild(createRunWithHistory(withoutReport, withReport), log);

        assertThat(referenceBuild).hasReferenceBuildId("report");
        assertThat(log.getInfoMessages()).contains(
                "Considering only builds that provide an action of type '%s'".formatted(
                        CoverageReportAction.class.getName()),
                "-> skipping build 'no-report' since it does not provide an action of type '%s'".formatted(
                        CoverageReportAction.class.getName()),
                "-> Previous build 'report' has a result SUCCESS");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldMatchTheRequiredActionTypeUsingTheSimpleClassName() {
        var withoutReport = createBuild("no-report", Result.SUCCESS);
        var withReport = createBuild("report", Result.SUCCESS, new CoverageReportAction("coverage"));

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredAction("CoverageReportAction");

        var referenceBuild = recorder.findReferenceBuild(
                createRunWithHistory(withoutReport, withReport), createLog());

        assertThat(referenceBuild).hasReferenceBuildId("report");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldMatchSuperClassesAndInterfacesOfTheRequiredActionType() {
        var withoutReport = createBuild("no-report", Result.SUCCESS);
        var withReport = createBuild("report", Result.SUCCESS,
                new CoverageReportAction.MutationCoverageReportAction("mutation-coverage"));

        var superClassRecorder = new SimpleReferenceRecorder();
        superClassRecorder.setRequiredAction(CoverageReportAction.class.getName());

        assertThat(superClassRecorder.findReferenceBuild(
                createRunWithHistory(withoutReport, withReport), createLog()))
                .hasReferenceBuildId("report");

        var interfaceRecorder = new SimpleReferenceRecorder();
        interfaceRecorder.setRequiredAction(ModelObject.class.getName()); // implemented by Action

        assertThat(interfaceRecorder.findReferenceBuild(
                createRunWithHistory(withoutReport, withReport), createLog()))
                .hasReferenceBuildId("report");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldSelectTheBuildThatProvidesAnActionWithTheRequiredId() {
        var mutationCoverage = createBuild("mutation", Result.SUCCESS, new CoverageReportAction("mutation-coverage"));
        var codeCoverage = createBuild("code", Result.SUCCESS, new CoverageReportAction("code-coverage"));

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredActionId("code-coverage");

        var log = createLog();
        var referenceBuild = recorder.findReferenceBuild(
                createRunWithHistory(mutationCoverage, codeCoverage), log);

        assertThat(referenceBuild).hasReferenceBuildId("code");
        assertThat(log.getInfoMessages()).contains(
                "Considering only builds that provide an action with ID 'code-coverage'",
                "-> skipping build 'mutation' since it does not provide an action with ID 'code-coverage'");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldCombineTheRequiredActionTypeAndId() {
        var wrongId = createBuild("wrong-id", Result.SUCCESS, new CoverageReportAction("mutation-coverage"));
        var wrongType = createBuild("wrong-type", Result.SUCCESS);
        var matching = createBuild("matching", Result.SUCCESS, new CoverageReportAction("code-coverage"));

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredAction("CoverageReportAction");
        recorder.setRequiredActionId("code-coverage");

        var log = createLog();
        var referenceBuild = recorder.findReferenceBuild(
                createRunWithHistory(wrongId, wrongType, matching), log);

        assertThat(referenceBuild).hasReferenceBuildId("matching");
        assertThat(log.getInfoMessages()).contains(
                "Considering only builds that provide an action of type 'CoverageReportAction' with ID 'code-coverage'",
                "-> Previous build 'matching' has a result SUCCESS");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldCombineTheActionFilterWithTheRequiredResult() {
        var failedWithReport = createBuild("failed", Result.FAILURE, new CoverageReportAction("coverage"));
        var successfulWithoutReport = createBuild("no-report", Result.SUCCESS);
        var successfulWithReport = createBuild("report", Result.SUCCESS, new CoverageReportAction("coverage"));

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredResult(Result.SUCCESS);
        recorder.setRequiredAction("CoverageReportAction");

        var referenceBuild = recorder.findReferenceBuild(
                createRunWithHistory(failedWithReport, successfulWithoutReport, successfulWithReport), createLog());

        assertThat(referenceBuild).hasReferenceBuildId("report");
    }

    @Test
    @Issue("JENKINS-72825")
    void shouldFindNoReferenceBuildIfNoBuildProvidesTheRequiredAction() {
        var first = createBuild("first", Result.SUCCESS);
        var second = createBuild("second", Result.SUCCESS);

        var recorder = new SimpleReferenceRecorder();
        recorder.setRequiredAction("NotExistingAction");

        var log = createLog();
        var referenceBuild = recorder.findReferenceBuild(createRunWithHistory(first, second), log);

        assertThat(referenceBuild).doesNotHaveReferenceBuild();
        assertThat(log.getInfoMessages()).contains(
                "-> ignoring reference build 'first' or one of its predecessors since none have a result of "
                        + "UNSTABLE or better and provide an action of type 'NotExistingAction'");
    }

    /**
     * Creates a run of a job that has the specified builds in its history: the first build of the array is the last
     * completed build, all other builds are the predecessors (in the given order).
     *
     * @param builds
     *         the builds in the history of the reference job
     *
     * @return the current run that will search for a reference build in the history of its own job
     */
    private Run<?, ?> createRunWithHistory(final FreeStyleBuild... builds) {
        var job = mock(FreeStyleProject.class);
        when(job.getDisplayName()).thenReturn("reference");
        when(job.getLastCompletedBuild()).thenReturn(builds[0]);
        for (var i = 0; i < builds.length - 1; i++) {
            when(builds[i].getPreviousCompletedBuild()).thenReturn(builds[i + 1]);
        }

        var run = mock(Run.class);
        when(run.getParent()).thenAnswer(i -> job);
        return run;
    }

    private FreeStyleBuild createBuild(final String id, final Result result, final Action... actions) {
        FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getResult()).thenReturn(result);
        when(build.getDisplayName()).thenReturn(id);
        when(build.getExternalizableId()).thenReturn(id);
        when(build.getAllActions()).thenAnswer(i -> List.of(actions));
        return build;
    }

    private FilteredLog createLog() {
        return new FilteredLog("test");
    }

    /**
     * Simulates a report that is not recorded in every build (like the coverage report of the coverage plugin). The ID
     * of the report is exposed as the URL name of this action.
     *
     * @author Akash Manna
     */
    static class CoverageReportAction implements Action {
        private final String id;

        CoverageReportAction(final String id) {
            this.id = id;
        }

        @Override @CheckForNull
        public String getIconFileName() {
            return null;
        }

        @Override @CheckForNull
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return id;
        }

        /** Verifies that the supertypes of an action are considered by the filter as well. */
        static final class MutationCoverageReportAction extends CoverageReportAction {
            MutationCoverageReportAction(final String id) {
                super(id);
            }
        }
    }
}
