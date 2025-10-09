package io.jenkins.plugins.forensics.reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

@WithJenkins
class Java25ITest {
    private JenkinsRule jenkins;

    @BeforeEach
    @SuppressWarnings("checkstyle:HiddenField")
    public void setUp(final JenkinsRule jenkins) {
        this.jenkins = jenkins;
    }

    @Test
    void shouldOverwriteReferenceBuild() throws Exception {
        var reference = jenkins.createProject(WorkflowJob.class);
        reference.setDefinition(new CpsFlowDefinition(
                """
                node {
                    echo 'Hello Job';
                    discoverReferenceBuild();
                }
                """, true));
        jenkins.buildAndAssertSuccess(reference);
    }
}
