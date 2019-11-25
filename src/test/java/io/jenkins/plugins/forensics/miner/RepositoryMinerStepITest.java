package io.jenkins.plugins.forensics.miner;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.forensics.util.IntegrationTestWithJenkinsPerSuite;

/**
 * Integration tests for the {@link RepositoryMinerStep}.
 *
 * @author Ullrich Hafner
 */
public class RepositoryMinerStepITest extends IntegrationTestWithJenkinsPerSuite {
    /** Loads the web page with enabled JS. */
    @Test
    public void shouldLoadJS() {
        FreeStyleProject job = createFreeStyleProject();

        job.getPublishersList().add(new RepositoryMinerStep());

        Run<?, ?> build = buildSuccessfully(job);

        getWebPage(JavaScriptSupport.JS_ENABLED, build, "forensics");
    }
}
