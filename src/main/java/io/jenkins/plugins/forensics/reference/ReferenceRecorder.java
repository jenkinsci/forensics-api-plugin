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
 * Base class for recorders that find reference builds.
 * <p>
 * Several plugins that report build statistics (test results, code coverage, metrics, static analysis warnings)
 * typically show their reports in two different ways: either as absolute report (e.g., total number of tests or
 * warnings, overall code coverage) or as relative delta report (e.g., additional tests, increased or decreased
 * coverage, new or fixed warnings). In order to compute a relative delta report a plugin needs to carefully select the
 * other build to compare the current results to (a so called reference build). For simple Jenkins jobs that build the
 * main branch of an SCM the reference build will be selected from one of the previous builds of the same job. For more
 * complex branch source projects (i.e., projects that build several branches and pull requests in a connected job
 * hierarchy) it makes more sense to select a reference build from a job that builds the actual target branch (i.e., the
 * branch the current changes will be merged into). Here one typically is interested what changed in a branch or pull
 * request with respect to the main branch (or any other target branch): e.g., how will the code coverage change if the
 * team merges the changes. Selecting the correct reference build is not that easy, since the main branch of a project
 * will evolve more frequently than a specific feature or bugfix branch.
 * </p>
 *
 * <p>
 * This recorder unifies the computation of the reference build so consuming plugins can simply use the resulting {@link
 * ReferenceBuild} in order to get a reference for their delta reports.
 * </p>
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
     * Sets the reference job: this job will be used as baseline to search for the best matching reference build. If
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
        return (ReferenceRecorderDescriptor) super.getDescriptor();
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
        Optional<Job<?, ?>> actualReferenceJob = findReferenceJob(run, log);
        if (actualReferenceJob.isPresent()) {
            Job<?, ?> reference = actualReferenceJob.get();
            Run<?, ?> lastCompletedBuild = reference.getLastCompletedBuild();
            if (lastCompletedBuild == null) {
                log.logInfo("No completed build found");
            }
            else {
                Optional<Run<?, ?>> referenceBuild = find(run, lastCompletedBuild);
                if (referenceBuild.isPresent()) {
                    Run<?, ?> result = referenceBuild.get();
                    log.logInfo("Found reference build '%s' for target branch", result.getDisplayName());

                    return new ReferenceBuild(run, log.getInfoMessages(), result);
                }
                log.logInfo("No reference build found that contains matching commits");
                if (isLatestBuildIfNotFound()) {
                    log.logInfo("Falling back to latest build of reference job: '%s'",
                            lastCompletedBuild.getDisplayName());

                    return new ReferenceBuild(run, log.getInfoMessages(), lastCompletedBuild);
                }
            }
        }
        return new ReferenceBuild(run, log.getInfoMessages());
    }

    protected abstract Optional<Run<?, ?>> find(Run<?, ?> run, Run<?, ?> lastCompletedBuildOfReferenceJob);

    private Optional<Job<?, ?>> findReferenceJob(final Run<?, ?> run, final FilteredLog log) {
        String jobName = getReferenceJob();
        if (isValidJobName(jobName)) {
            log.logInfo("Configured reference job: '%s'", jobName);

            return findJob(jobName, log);
        }
        else {
            Job<?, ?> job = run.getParent();
            ItemGroup<?> topLevel = job.getParent();
            if (topLevel instanceof MultiBranchProject) {
                // TODO: we should make use of the branch API
                if (getReferenceBranch().equals(job.getName())) {
                    log.logInfo("No reference job required - we are already on the default branch for '%s'",
                            job.getName());
                }
                else {
                    log.logInfo("Reference job inferred from toplevel project '%s'", topLevel.getDisplayName());
                    String referenceFromDefaultBranch = job.getParent().getFullName() + "/" + getReferenceBranch();
                    log.logInfo("Target branch: '%s'", getReferenceBranch());
                    log.logInfo("Inferred job name: '%s'", referenceFromDefaultBranch);

                    return findJob(referenceFromDefaultBranch, log);
                }
            }
            else {
                log.logInfo("Consider configuring a reference job using the 'referenceJob' property");
            }
            return Optional.empty();
        }
    }

    private Optional<Job<?, ?>> findJob(final String jobName, final FilteredLog log) {
        Optional<Job<?, ?>> job = jenkins.getJob(jobName);
        if (!job.isPresent()) {
            log.logInfo("There is no such job - maybe the job has been renamed or deleted?");
        }
        return job;
    }

    private boolean isValidJobName(final String name) {
        return StringUtils.isNotBlank(name) && !NO_REFERENCE_JOB.equals(name);
    }

    /**
     * Descriptor for this step: defines the context and the UI data binding and validation.
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
        public ComboBoxModel doFillReferenceJobItems() {
            return model.getAllJobs();
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param referenceJob
         *         the reference job
         *
         * @return the validation result
         */
        @SuppressWarnings("unused") // Used in jelly validation
        public FormValidation doCheckReferenceJob(@QueryParameter final String referenceJob) {
            return model.validateJob(referenceJob);
        }
    }
}
