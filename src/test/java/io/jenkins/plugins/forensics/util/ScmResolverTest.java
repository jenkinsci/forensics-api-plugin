package io.jenkins.plugins.forensics.util;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ScmResolver}.
 *
 * @author Andreas Reiser
 */
class ScmResolverTest {
    @Test
    void shouldCreateNullBlamerOnNullScm() {
        assertThatScm(mock(Run.class)).isInstanceOf(NullSCM.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScm() {
        AbstractProject job = mock(AbstractProject.class);
        SCM scm = mock(SCM.class);
        when(job.getScm()).thenReturn(scm);

        AbstractBuild build = createBuildFor(job);
        assertThatScm(build).isInstanceOf(SCM.class);
    }

    @Test
    void shouldCreateGitBlamerOnGitScmOnRoot() {
        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        SCM gitScm = mock(SCM.class);
        when(root.getScm()).thenReturn(gitScm);

        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);
        assertThatScm(build).isInstanceOf(SCM.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateGitBlamerForPipeline() {
        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        SCM gitScm = mock(SCM.class);
        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(asSingleton(gitScm));

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(SCM.class);
    }

    @Test
    void shouldCreateGitBlamerForPipelineWithFlowNode() throws IOException {
        WorkflowJob pipeline = createPipeline();
        pipeline.setDefinition(createCpsFlowDefinition());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(SCM.class);
    }

    private CpsScmFlowDefinition createCpsFlowDefinition() {
        CpsScmFlowDefinition flowDefinition = mock(CpsScmFlowDefinition.class);
        SCM git = mock(SCM.class);
        when(flowDefinition.getScm()).thenReturn(git);
        return flowDefinition;
    }

    @SuppressWarnings("unchecked")
    private WorkflowJob createPipeline() throws IOException {
        ItemGroup group = mock(ItemGroup.class);
        WorkflowJob pipeline = new WorkflowJob(group, "stub");
        when(group.getRootDirFor(any())).thenReturn(Files.createTempFile("", "").toFile());
        when(group.getFullName()).thenReturn("bla");
        return pipeline;
    }

    @Test
    void shouldCreateNullBlamerForPipelineWithNoScm() {
        Job pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(new ArrayList<>());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScm(run).isInstanceOf(NullSCM.class);
    }

    private List asSingleton(final SCM gitScm) {
        return Collections.singletonList(gitScm);
    }

    @Test
    void shouldCreateNullBlamerIfNeitherProjectNorRootHaveScm() {
        AbstractProject job = mock(AbstractProject.class);

        AbstractProject root = mock(AbstractProject.class);
        when(job.getRootProject()).thenReturn(root);

        AbstractBuild build = createBuildFor(job);

        assertThatScm(build).isInstanceOf(NullSCM.class);
    }

    private ObjectAssert<SCM> assertThatScm(final Run run) {
        return assertThat(new ScmResolver().getScm(run));
    }

    private Run<?, ?> createRunFor(final Job<?, ?> job) {
        Run build = mock(Run.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private AbstractBuild createBuildFor(final AbstractProject job) {
        AbstractBuild build = mock(AbstractBuild.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private void createRunWithEnvironment(final Job<?, ?> job, final Run build) {
        try {
            EnvVars environment = mock(EnvVars.class);
            when(build.getEnvironment(TaskListener.NULL)).thenReturn(environment);

            when(build.getParent()).thenReturn(job);
        }
        catch (IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }
}
