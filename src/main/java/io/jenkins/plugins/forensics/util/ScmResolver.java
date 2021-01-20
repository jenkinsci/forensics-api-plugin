package io.jenkins.plugins.forensics.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.triggers.SCMTriggerItem;

/**
 * Resolves the used SCM in a given build.
 *
 * @author Ullrich Hafner
 */
public class ScmResolver {
    private static final List<SCM> EMPTY = Collections.emptyList();

    /**
     * Returns the SCM in a given build. If no SCM can be determined, then a {@link NullSCM} instance will be returned.
     *
     * @param run
     *         the build to get the SCM from
     *
     * @return the SCM
     */
    public SCM getScm(final Run<?, ?> run) {
        Collection<? extends SCM> scms = getScms(run);
        if (scms.isEmpty()) {
            return new NullSCM();
        }
        return scms.iterator().next();
    }

    /**
     * Returns the SCMs in a given build, filtered by the name.
     *
     * @param run
     *         the build to get the SCMs from
     * @param keyFilter
     *         substring that must be part of the SCM key
     *
     * @return the SCMs
     */
    public Collection<? extends SCM> getScms(final Run<?, ?> run, final String keyFilter) {
        return getScms(run)
                .stream()
                .filter(r -> r.getKey().contains(keyFilter))
                .collect(Collectors.toList());
    }

    /**
     * Returns the SCMs in a given build.
     *
     * @param run
     *         the build to get the SCMs from
     *
     * @return the SCMs
     */
    public Collection<? extends SCM> getScms(final Run<?, ?> run) {
        if (run instanceof AbstractBuild) {
            return extractFromProject((AbstractBuild<?, ?>) run);
        }
        if (run instanceof WorkflowRun) {
            List<SCM> scms = ((WorkflowRun) run).getSCMs();
            if (!scms.isEmpty()) {
                return scms;
            }
        }
        Job<?, ?> job = run.getParent();
        if (job instanceof SCMTriggerItem) {
            return extractFromPipeline(job);
        }
        return EMPTY;
    }

    private Collection<? extends SCM> extractFromPipeline(final Job<?, ?> job) {
        Collection<? extends SCM> scms = ((SCMTriggerItem) job).getSCMs();
        if (!scms.isEmpty()) {
            return scms;
        }

        if (job instanceof WorkflowJob) {
            FlowDefinition definition = ((WorkflowJob) job).getDefinition();
            if (definition instanceof CpsScmFlowDefinition) {
                return asCollection(((CpsScmFlowDefinition) definition).getScm());
            }
        }

        return EMPTY;
    }

    private Collection<? extends SCM> extractFromProject(final AbstractBuild<?, ?> run) {
        AbstractProject<?, ?> project = run.getProject();
        if (project.getScm() != null) {
            return asCollection(project.getScm());
        }

        SCM scm = project.getRootProject().getScm();
        if (scm != null) {
            return asCollection(scm);
        }

        return EMPTY;
    }

    private Set<SCM> asCollection(final SCM scm) {
        return Collections.singleton(scm);
    }
}
