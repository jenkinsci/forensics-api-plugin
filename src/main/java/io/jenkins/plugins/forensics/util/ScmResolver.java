package io.jenkins.plugins.forensics.util;

import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
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
                .filter(r -> Strings.CI.contains(r.getKey(), keyFilter))
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
        Collection<? extends SCM> allScms = findScms(run);
        Set<String> ids = allScms.stream().map(SCM::getKey).collect(Collectors.toSet());
        List<SCM> uniqueScms = new ArrayList<>();
        for (SCM scm : allScms) {
            if (ids.contains(scm.getKey())) {
                uniqueScms.add(scm);
                ids.remove(scm.getKey());
            }
        }
        return uniqueScms;
    }

    private Collection<? extends SCM> findScms(final Run<?, ?> run) {
        if (run instanceof AbstractBuild<?, ?> build) {
            return extractFromProject(build);
        }
        if (run instanceof WorkflowRun workflowRun) {
            List<SCM> scms = workflowRun.getSCMs();
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

        if (job instanceof WorkflowJob workflowJob) {
            var definition = workflowJob.getDefinition();
            if (definition instanceof CpsScmFlowDefinition flowDefinition) {
                return asCollection(flowDefinition.getScm());
            }
        }

        return EMPTY;
    }

    private Collection<? extends SCM> extractFromProject(final AbstractBuild<?, ?> run) {
        AbstractProject<?, ?> project = run.getParent();
        if (project.getScm() != null) {
            return asCollection(project.getScm());
        }

        var scm = project.getRootProject().getScm();
        if (scm != null) {
            return asCollection(scm);
        }

        return EMPTY;
    }

    private Set<SCM> asCollection(final SCM scm) {
        return Set.of(scm);
    }
}
