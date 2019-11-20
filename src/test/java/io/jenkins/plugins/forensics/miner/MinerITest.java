package io.jenkins.plugins.forensics.miner;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.forensics.util.IntegrationTestWithJenkinsPerSuite;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class MinerITest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    public void shouldLoadJS() {
        FreeStyleProject job = createFreeStyleProject();

        job.getPublishersList().add(new RepositoryMinerStep());

        Run<?, ?> build = buildSuccessfully(job);

        HtmlPage buildOverview = getWebPage(JavaScriptSupport.JS_ENABLED, build, "forensics");

        System.out.println(buildOverview.asText());
    }
}
