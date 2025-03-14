package io.jenkins.plugins.forensics.util;

import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
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
        assertThatScmOf(mock(Run.class)).isInstanceOf(NullSCM.class);
    }

    @Test
    void shouldResolveScmOnGitScm() {
        AbstractProject<?, ?> job = mock(AbstractProject.class);
        SCM scm = mock(SCM.class);
        when(job.getScm()).thenReturn(scm);

        AbstractBuild<?, ?> build = createBuildFor(job);
        assertThatScmOf(build).isInstanceOf(SCM.class);
    }

    @Test
    void shouldResolveScmOnGitScmOnRoot() {
        AbstractProject<?, ?> job = mock(AbstractProject.class);

        AbstractProject<?, ?> root = mock(AbstractProject.class);
        SCM gitScm = mock(SCM.class);
        when(root.getScm()).thenReturn(gitScm);

        when(job.getRootProject()).thenAnswer(i -> root);

        AbstractBuild<?, ?> build = createBuildFor(job);
        assertThatScmOf(build).isInstanceOf(SCM.class);
    }

    @Test
    void shouldResolveScmForPipeline() {
        WorkflowRun build = mock(WorkflowRun.class);
        SCM gitScm = mock(SCM.class);
        when(build.getSCMs()).thenAnswer(i -> Collections.singletonList(gitScm));
        assertThatScmOf(build).isInstanceOf(SCM.class);
    }

    @Test
    void shouldResolveScmForSCMTriggerItem() {
        Job<?, ?> pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        SCM gitScm = mock(SCM.class);
        when(((SCMTriggerItem) pipeline).getSCMs()).thenAnswer(i -> Collections.singletonList(gitScm));

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScmOf(run).isInstanceOf(SCM.class);
    }

    @Test
    void shouldSkipDuplicateScms() {
        Job<?, ?> pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        var first = createScm("key");
        var second = createScm("otherKey");
        when(((SCMTriggerItem) pipeline).getSCMs()).thenAnswer(i -> Arrays.asList(first, second));

        Run<?, ?> run = createRunFor(pipeline);
        assertThat(new ScmResolver().getScms(run)).hasSize(2);

        when(((SCMTriggerItem) pipeline).getSCMs()).thenAnswer(i -> Arrays.asList(first, first));

        Run<?, ?> duplicates = createRunFor(pipeline);
        assertThat(new ScmResolver().getScms(duplicates)).hasSize(1);
    }

    @Test
    void shouldFilterScms() {
        Job<?, ?> pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        var first = createScm("key");
        var second = createScm("otherKey");
        when(((SCMTriggerItem) pipeline).getSCMs()).thenAnswer(i -> List.of(first, second));

        Run<?, ?> run = createRunFor(pipeline);
        assertThat(new ScmResolver().getScms(run, "ey")).hasSize(2);
        assertThat(new ScmResolver().getScms(run, "other")).hasSize(1);
        assertThat(new ScmResolver().getScms(run, "erK")).hasSize(1);
    }

    @ParameterizedTest(name = "Filter repository by case insensitive name {0}")
    @ValueSource(strings = {"git", "repository", "GIT", "REPOSITORY", "git-repository", "Git-Repository"})
    void shouldFilterScmCaseInsensitive(final String filter) {
        Job<?, ?> pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        var scm = createScm("git-repository");
        var other = createScm("other");
        when(((SCMTriggerItem) pipeline).getSCMs()).thenAnswer(i -> List.of(scm, other));

        Run<?, ?> run = createRunFor(pipeline);
        assertThat(new ScmResolver().getScms(run, filter)).hasSize(1);
        assertThat(new ScmResolver().getScms(run, "other")).hasSize(1);
    }

    private SCM createScm(final String key) {
        SCM first = mock(SCM.class);
        when(first.getKey()).thenReturn(key);
        return first;
    }

    @Test
    void shouldResolveScmForPipelineWithFlowNode() {
        var pipeline = mock(WorkflowJob.class);

        var empty = createRunFor(pipeline);
        assertThatScmOf(empty).isInstanceOf(NullSCM.class);

        var flowDefinition = createCpsFlowDefinition();
        when(pipeline.getDefinition()).thenReturn(flowDefinition);

        var withScmFromFlowDefinition = createRunFor(pipeline);
        assertThatScmOf(withScmFromFlowDefinition).isInstanceOf(SCM.class);
    }

    private CpsScmFlowDefinition createCpsFlowDefinition() {
        CpsScmFlowDefinition flowDefinition = mock(CpsScmFlowDefinition.class);
        SCM git = mock(SCM.class);
        when(flowDefinition.getScm()).thenReturn(git);
        return flowDefinition;
    }

    @Test
    void shouldCreateNullBlamerForPipelineWithNoScm() {
        Job<?, ?> pipeline = mock(Job.class, withSettings().extraInterfaces(SCMTriggerItem.class));

        when(((SCMTriggerItem) pipeline).getSCMs()).thenReturn(new ArrayList<>());

        Run<?, ?> run = createRunFor(pipeline);
        assertThatScmOf(run).isInstanceOf(NullSCM.class);
    }

    @Test
    void shouldCreateNullBlamerIfNeitherProjectNorRootHaveScm() {
        AbstractProject<?, ?> job = mock(AbstractProject.class);

        AbstractProject<?, ?> root = mock(AbstractProject.class);
        when(job.getRootProject()).thenAnswer(i -> root);

        AbstractBuild<?, ?> build = createBuildFor(job);

        assertThatScmOf(build).isInstanceOf(NullSCM.class);
    }

    private ObjectAssert<SCM> assertThatScmOf(final Run<?, ?> run) {
        return assertThat(new ScmResolver().getScm(run));
    }

    private Run<?, ?> createRunFor(final Job<?, ?> job) {
        Run<?, ?> build = mock(Run.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private AbstractBuild<?, ?> createBuildFor(final AbstractProject<?, ?> job) {
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);
        createRunWithEnvironment(job, build);
        return build;
    }

    private void createRunWithEnvironment(final Job<?, ?> job, final Run<?, ?> build) {
        try {
            EnvVars environment = mock(EnvVars.class);
            when(build.getEnvironment(TaskListener.NULL)).thenReturn(environment);

            when(build.getParent()).thenAnswer(i -> job);
        }
        catch (IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }
}
