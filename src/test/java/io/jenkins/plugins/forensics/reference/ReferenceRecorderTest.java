package io.jenkins.plugins.forensics.reference;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;

import io.jenkins.plugins.forensics.reference.ReferenceRecorder.ScmFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceRecorder}.
 *
 * @author Ullrich Hafner
 */
class ReferenceRecorderTest {
    @Test
    void shouldCreateReferenceRecorder() {
        Run<?, ?> build = mock(Run.class);

        Job<?, ?> job = mock(Job.class);
        when(build.getParent()).thenAnswer(i -> job);

        WorkflowMultiBranchProject parent = mock(WorkflowMultiBranchProject.class);
        when(job.getParent()).thenAnswer(i -> parent);

        ReferenceRecorder recorder = mock(ReferenceRecorder.class, CALLS_REAL_METHODS);
        FilteredLog log = new FilteredLog("EMPTY");
        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).startsWith("Found a `MultiBranchProject`"));

        assertThat(referenceBuild.getReferenceBuild()).isEmpty();
    }

    @Test
    void shouldObtainReferenceFromGivenTargetBranch() {
        Run<?, ?> build = mock(Run.class);

        Job<?, ?> job = mock(Job.class);
        when(build.getParent()).thenAnswer(i -> job);

        WorkflowMultiBranchProject parent = mock(WorkflowMultiBranchProject.class);
        when(job.getParent()).thenAnswer(i -> parent);

        Job<?, ?> targetJob = mock(Job.class);
        when(parent.getItemByBranchName("target")).thenAnswer(i-> targetJob);

        Run<?, ?> targetBuild = mock(Run.class);
        when(targetBuild.getExternalizableId()).thenReturn("target-id");

        when(targetJob.getLastCompletedBuild()).thenAnswer(i -> targetBuild);

        ReferenceRecorder recorder = mock(ReferenceRecorder.class, CALLS_REAL_METHODS);
        recorder.setTargetBranch("target");

        when(recorder.find(build, targetBuild)).thenReturn(Optional.of(targetBuild));

        FilteredLog log = new FilteredLog("EMPTY");
        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).startsWith("Found a `MultiBranchProject`"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("target-id");
    }

    @Test
    void shouldObtainReferenceFromPullRequestTarget() {
        Run<?, ?> build = mock(Run.class);

        Job<?, ?> job = mock(Job.class);
        when(build.getParent()).thenAnswer(i -> job);

        WorkflowMultiBranchProject parent = mock(WorkflowMultiBranchProject.class);
        when(job.getParent()).thenAnswer(i -> parent);

        Job<?, ?> targetJob = mock(Job.class);
        when(parent.getItemByBranchName("target")).thenAnswer(i-> targetJob);
        Job<?, ?> prJob = mock(Job.class);
        when(parent.getItemByBranchName("pr-target")).thenAnswer(i-> prJob);

        Run<?, ?> targetBuild = mock(Run.class);
        when(targetBuild.getExternalizableId()).thenReturn("target-id");
        when(targetJob.getLastCompletedBuild()).thenAnswer(i -> targetBuild);

        Run<?, ?> prBuild = mock(Run.class);
        when(prBuild.getExternalizableId()).thenReturn("pr-id");
        when(prJob.getLastCompletedBuild()).thenAnswer(i -> prBuild);

        ReferenceRecorder recorder = mock(ReferenceRecorder.class, CALLS_REAL_METHODS);
        recorder.setTargetBranch("target");
        ScmFacade scmFacade = mock(ScmFacade.class);
        SCMHead pr = mock(SCMHead.class, withSettings().extraInterfaces(ChangeRequestSCMHead.class));
        when(pr.toString()).thenReturn("pr");
        when(scmFacade.findHead(job)).thenAnswer(i -> pr);
        SCMHead target = mock(SCMHead.class);
        when(((ChangeRequestSCMHead) pr).getTarget()).thenReturn(target);
        when(target.getName()).thenReturn("pr-target");

        when(recorder.getScmFacade()).thenReturn(scmFacade);

        when(recorder.find(build, prBuild)).thenReturn(Optional.of(prBuild));

        FilteredLog log = new FilteredLog("EMPTY");
        ReferenceBuild referenceBuild = recorder.findReferenceBuild(build, log);

        assertThat(log.getInfoMessages())
                .anySatisfy(m -> assertThat(m).startsWith("-> detected a pull or merge request 'pr' for target branch 'pr-target'"));

        assertThat(referenceBuild.getReferenceBuildId()).isEqualTo("pr-id");
    }
}
