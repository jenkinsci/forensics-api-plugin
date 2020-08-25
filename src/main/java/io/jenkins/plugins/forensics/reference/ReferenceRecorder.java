package io.jenkins.plugins.forensics.reference;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import jenkins.branch.MultiBranchProject;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;

/**
 * Base class for recorders that find a build in a reference job that matches best with the current build of a given
 * job.
 *
 * @author Arne Schöntag
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public abstract class ReferenceRecorder extends Recorder implements SimpleBuildStep {
    static final String NO_REFERENCE_JOB = "-";

    private static final String DEFAULT_BRANCH = "master";

    private final JenkinsFacade jenkins;

    private String referenceJob = StringUtils.EMPTY;
    private boolean latestBuildIfNotFound = false;
    private String defaultBranch = DEFAULT_BRANCH;

    /**
     * Creates a new instance of {@link ReferenceRecorder}.
     *
     * @param jenkins
     *         facade to Jenkins
     */
    protected ReferenceRecorder(final JenkinsFacade jenkins) {
        super();

        this.jenkins = jenkins;
    }

    /**
     * Sets the reference job: this job will be used as base line to search for the best matching reference build. If
     * the reference job should be computed automatically (supported by {@link MultiBranchProject multi-branch projects}
     * only), then let this field empty.
     *
     * @param referenceJob
     *         the name of reference job
     */
    @DataBoundSetter
    public void setReferenceJob(final String referenceJob) {
        if (NO_REFERENCE_JOB.equals(referenceJob)) {
            this.referenceJob = StringUtils.EMPTY;
        }
        this.referenceJob = StringUtils.strip(referenceJob);
    }

    /**
     * Returns the name of the reference job. If the job is not defined, then {@link #NO_REFERENCE_JOB} is returned.
     *
     * @return the name of reference job, or {@link #NO_REFERENCE_JOB} if undefined
     */
    public String getReferenceJob() {
        if (StringUtils.isBlank(referenceJob)) {
            return NO_REFERENCE_JOB;
        }
        return referenceJob;
    }

    /**
     * If enabled, then the latest build of the reference job will be used if no other reference build has been found.
     *
     * @param latestBuildIfNotFound
     *         if {@code true} then the latest build of the reference job will be used if no matching reference build
     *         has been found, otherwise no reference build is returned.
     */
    @DataBoundSetter
    public void setLatestBuildIfNotFound(final boolean latestBuildIfNotFound) {
        this.latestBuildIfNotFound = latestBuildIfNotFound;
    }

    public boolean isLatestBuildIfNotFound() {
        return latestBuildIfNotFound;
    }

    /**
     * Sets the default branch for {@link MultiBranchProject multi-branch projects}: the default branch is considered
     * the base branch in your repository. The builds of all other branches and pull requests will use this default
     * branch as baseline to search for a matching reference build.
     *
     * @param defaultBranch
     *         the name of the default branch
     */
    @DataBoundSetter
    public void setDefaultBranch(final String defaultBranch) {
        this.defaultBranch = StringUtils.stripToEmpty(defaultBranch);
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    private String getReferenceBranch() {
        return StringUtils.defaultIfBlank(StringUtils.strip(defaultBranch), DEFAULT_BRANCH);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public ReferenceRecorderDescriptor getDescriptor() {
        return (ReferenceRecorderDescriptor)super.getDescriptor();
    }

    @Override
    public void perform(@NonNull final Run<?, ?> run, @NonNull final FilePath workspace,
            @NonNull final Launcher launcher, @NonNull final TaskListener listener) {
        FilteredLog log = new FilteredLog("Errors while computing the reference build");

        run.addAction(findReferenceBuild(run, log));

        LogHandler logHandler = new LogHandler(listener, "ReferenceFinder");
        logHandler.log(log);
    }

    private ReferenceBuild findReferenceBuild(final Run<?, ?> run, final FilteredLog log) {
        Optional<Job<?, ?>> actualReferenceJob = getReferenceJob(run, log);
        if (actualReferenceJob.isPresent()) {
            Job<?, ?> reference = actualReferenceJob.get();
            log.logInfo("Finding reference build for `%s`", reference.getFullDisplayName());
            Run<?, ?> lastCompletedBuild = reference.getLastCompletedBuild();
            if (lastCompletedBuild == null) {
                log.logInfo("-> no completed build found");
            }
            else {
                Optional<Run<?, ?>> referenceBuild = find(run, lastCompletedBuild);
                if (referenceBuild.isPresent()) {
                    Run<?, ?> result = referenceBuild.get();
                    log.logInfo("-> found `%s`", result.getDisplayName());
                    return new ReferenceBuild(run, result);
                }
                log.logInfo("-> no reference build found that contains matching commits");
                if (isLatestBuildIfNotFound()) {
                    log.logInfo("-> using latest build of reference job `%s`", lastCompletedBuild.getDisplayName());

                    return new ReferenceBuild(run, lastCompletedBuild);
                }
            }
        }
        else {
            log.logInfo("Reference job '%s' not found", getReferenceJob());
        }
        return new ReferenceBuild(run);
    }

    protected abstract Optional<Run<?, ?>> find(Run<?, ?> run, Run<?, ?> lastCompletedBuildOfReferenceJob);

    private Optional<Job<?, ?>> getReferenceJob(final Run<?, ?> run, final FilteredLog log) {
        String jobName = getReferenceJob();
        if (isValidJobName(jobName)) {
            log.logInfo("Using configured reference job '%s'" + jobName);
            log.logInfo("-> " + jobName);
            return jenkins.getJob(jobName);
        }
        else {
            Job<?, ?> job = run.getParent();
            ItemGroup<?> topLevel = job.getParent();
            if (topLevel instanceof MultiBranchProject) {
                // TODO: we should make use of the branch API
                if (getReferenceBranch().equals(job.getName())) {
                    log.logInfo("No reference job obtained since we are already on the default branch '%s'",
                            job.getName());
                }
                else {
                    log.logInfo("Obtaining reference job name from toplevel item `%s`", topLevel.getDisplayName());
                    String referenceFromDefaultBranch = job.getParent().getFullName() + "/" + getReferenceBranch();
                    log.logInfo("-> job name: " + referenceFromDefaultBranch);
                    return jenkins.getJob(referenceFromDefaultBranch);
                }
            }
            return Optional.empty();
        }
    }

    private boolean isValidJobName(final String name) {
        return StringUtils.isNotBlank(name) && !NO_REFERENCE_JOB.equals(name);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    protected static class ReferenceRecorderDescriptor extends BuildStepDescriptor<Publisher> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Recorder_DisplayName();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        private final ReferenceJobModelValidation model = new ReferenceJobModelValidation();

        /**
         * Returns the model with the possible reference jobs.
         *
         * @return the model with the possible reference jobs
         */
        public ComboBoxModel doFillReferenceJobNameItems() {
            return model.getAllJobs();
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param referenceJobName
         *         the reference job
         *
         * @return the validation result
         */
        public FormValidation doCheckReferenceJobName(@QueryParameter final String referenceJobName) {
            return model.validateJob(referenceJobName);
        }
    }
}
