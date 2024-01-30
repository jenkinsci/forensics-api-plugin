package io.jenkins.plugins.forensics.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;

import io.jenkins.plugins.forensics.reference.ReferenceRecorder.ScmFacade;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceRecorder}.
 *
 * @author Ullrich Hafner
 */
class ReferenceRecorderTest {
    /**
     * Verifies that the reference recorder has no initial value for the default branch.
     */
    @Test
    void shouldInitCorrectly() {
        ReferenceRecorder recorder = new NullReferenceRecorder();

        assertThat(recorder)
                .hasTargetBranch(StringUtils.EMPTY)
                .hasScm(StringUtils.EMPTY);
    }

    /**
     * Verifies the first alternative: the current build is for a pull request part in a multi-branch project. In this
     *  case, the target branch stored in the PR will be used as the reference job.
     */
    @Test
    void shouldObtainReferenceFromPullRequestTarget() {
        FilteredLog log = createLog();

        Run<?, ?> build = mock(Run.class);
        Job<?, ?> job = createJob(build);
        WorkflowMultiBranchProject topLevel = createMultiBranch(job);

        ReferenceRecorder recorder = createSut();

        Run<?, ?> prBuild = configurePrJobAndBuild(recorder, topLevel, job);
        when(recorder.find(build, prBuild, log)).thenReturn(Optional.of(prBuild));

        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).contains("no target branch configured in step"))
                .anySatisfy(m -> assertThat(m).contains("detected a pull or merge request for target branch 'pr-target'"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("pr-id");
    }

    /**
     * Verifies the second alternative: the current build is part of a multi-branch project (but not for a PR).
     * Additionally, a primary branch has been configured for the multi-branch project using the action {@link
     * PrimaryInstanceMetadataAction}. In this case, this configured primary target will be used as the reference job.
     */
    @Test
    void shouldFindReferenceJobUsingPrimaryBranch() {
        FilteredLog log = createLog();

        Run<?, ?> build = mock(Run.class);
        Job<?, ?> job = createJob(build);
        WorkflowMultiBranchProject topLevel = createMultiBranch(job);

        ReferenceRecorder recorder = createSut();

        configurePrimaryBranch(recorder, topLevel, job, build, log);

        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(
                        m -> assertThat(m).contains("Found a `MultiBranchProject`"))
                .anySatisfy(
                        m -> assertThat(m).contains("using configured primary branch 'main' of SCM as target branch"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("main-id");
    }

    /**
     * Verifies the third alternative: the current build is for a pull request part in a multi-branch project. However,
     * the step has been configured with the parameter {@code targetBranch}. In this case the configured target branch
     * will override the target branch of the pull PR.
     */
    @Test
    void targetShouldHavePrecedenceBeforePullRequestTarget() {
        FilteredLog log = createLog();

        Run<?, ?> build = mock(Run.class);
        Job<?, ?> job = createJob(build);
        WorkflowMultiBranchProject topLevel = createMultiBranch(job);

        ReferenceRecorder recorder = createSut();

        configurePrJobAndBuild(recorder, topLevel, job); // will not be used since target branch has been set
        configureTargetJobAndBuild(recorder, topLevel, build, log);

        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).contains("using target branch 'target' as configured in step"))
                .noneSatisfy(m -> assertThat(m).contains("detected a pull or merge request"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("target-id");
    }

    /**
     * Verifies the fourth alternative: the current build is part of a multi-branch project (but not for a PR).
     * Additionally, a primary branch has been configured for the multi-branch project using the action {@link
     * PrimaryInstanceMetadataAction}. However, the step has been configured with the parameter {@code targetBranch}. In
     * this case the configured target branch will override the primary branch.
     */
    @Test
    void targetShouldHavePrecedenceBeforePrimaryBranchTarget() {
        FilteredLog log = createLog();

        Run<?, ?> build = mock(Run.class);
        Job<?, ?> job = createJob(build);
        WorkflowMultiBranchProject topLevel = createMultiBranch(job);

        ReferenceRecorder recorder = createSut();

        configurePrimaryBranch(recorder, topLevel, job, build, log); // will not be used since target branch has been set
        configureTargetJobAndBuild(recorder, topLevel, build, log);

        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).contains("using target branch 'target' as configured in step"))
                .noneSatisfy(m -> assertThat(m).contains("detected a pull or merge request"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("target-id");
    }

    /**
     * Verifies the fallback alternative: neither a PR, nor a primary branch, nor a target branch parameter are set. In
     * this case the default target branch {@code master} will be used.
     */
    @Test
    void shouldNotFindReferenceJobForMultiBranchProject() {
        FilteredLog log = createLog();

        Run<?, ?> build = mock(Run.class);
        Job<?, ?> job = createJob(build);
        createMultiBranch(job);

        ReferenceRecorder recorder = createSut();
        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).contains("Found a `MultiBranchProject`"))
                .anySatisfy(m -> assertThat(m).contains("falling back to plugin default target branch 'master'"));

        assertThat(referenceBuild.getReferenceBuild()).isEmpty();
    }

    private ReferenceRecorder createSut() {
        return mock(ReferenceRecorder.class, CALLS_REAL_METHODS);
    }

    private FilteredLog createLog() {
        return new FilteredLog("EMPTY");
    }

    private WorkflowMultiBranchProject createMultiBranch(final Job<?, ?> job) {
        WorkflowMultiBranchProject parent = mock(WorkflowMultiBranchProject.class);
        when(job.getParent()).thenAnswer(i -> parent);
        return parent;
    }

    private Job<?, ?> createJob(final Run<?, ?> build) {
        Job<?, ?> job = mock(Job.class);
        when(build.getParent()).thenAnswer(i -> job);
        return job;
    }

    private void configurePrimaryBranch(final ReferenceRecorder recorder,
            final WorkflowMultiBranchProject topLevel, final Job<?, ?> job,
            final Run<?, ?> build, final FilteredLog log) {
        Job<?, ?> main = mock(Job.class);
        Run<?, ?> mainBuild = mock(Run.class);
        when(mainBuild.getExternalizableId()).thenReturn("main-id");
        when(main.getLastCompletedBuild()).thenAnswer(i -> mainBuild);
        when(main.getDisplayName()).thenReturn("main");

        when(main.getAction(PrimaryInstanceMetadataAction.class)).thenReturn(mock(PrimaryInstanceMetadataAction.class));

        Item item = mock(Item.class);
        when(item.getAllJobs()).thenAnswer(i -> Arrays.<Job<?, ?>>asList(job, main));

        when(topLevel.getAllItems()).thenReturn(Collections.singletonList(item));

        when(recorder.find(build, mainBuild, log)).thenReturn(Optional.of(mainBuild));
    }

    private Run<?, ?> configurePrJobAndBuild(final ReferenceRecorder recorder,
            final WorkflowMultiBranchProject parent, final Job<?, ?> job) {
        Job<?, ?> prJob = mock(Job.class);
        when(parent.getItemByBranchName("pr-target")).thenAnswer(i -> prJob);
        Run<?, ?> prBuild = mock(Run.class);
        when(prBuild.getExternalizableId()).thenReturn("pr-id");
        when(prJob.getLastCompletedBuild()).thenAnswer(i -> prBuild);

        ScmFacade scmFacade = mock(ScmFacade.class);
        ChangeRequestSCMHead pr = mock(ChangeRequestSCMHead.class);
        when(pr.toString()).thenReturn("pr");
        when(scmFacade.findHead(job)).thenReturn(Optional.of(pr));
        SCMHead target = mock(SCMHead.class);
        when(pr.getTarget()).thenReturn(target);
        when(target.getName()).thenReturn("pr-target");

        when(recorder.getScmFacade()).thenReturn(scmFacade);

        return prBuild;
    }

    private Run<?, ?> configureTargetJobAndBuild(final ReferenceRecorder recorder,
            final WorkflowMultiBranchProject parent, final Run<?, ?> build, final FilteredLog log) {
        recorder.setTargetBranch("target");

        Job<?, ?> targetJob = mock(Job.class);
        when(parent.getItemByBranchName("target")).thenAnswer(i -> targetJob);
        Run<?, ?> targetBuild = mock(Run.class);
        when(targetBuild.getExternalizableId()).thenReturn("target-id");
        when(targetJob.getLastCompletedBuild()).thenAnswer(i -> targetBuild);

        when(recorder.find(build, targetBuild, log)).thenReturn(Optional.of(targetBuild));

        return targetBuild;
    }

    private static class NullReferenceRecorder extends ReferenceRecorder {
        @VisibleForTesting
        NullReferenceRecorder() {
            super(new JenkinsFacade());
        }
    }
}
