package io.jenkins.plugins.forensics.reference;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.branch.MultiBranchProject;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;

/**
 * A simple recorder that discovers a reference build from another given reference job.
 *
 * <p>
 * Several plugins that report build statistics (test results, code coverage, metrics, static analysis warnings)
 * typically show their reports in two different ways: either as absolute report (e.g., total number of tests or
 * warnings, overall code coverage) or as relative delta report (e.g., additional tests, increased or decreased
 * coverage, new or fixed warnings). To compute a relative delta report, a plugin needs to carefully select the
 * other build to compare the current results to (a so-called reference build). For simple Jenkins jobs that build the
 * main branch of an SCM, the reference build will be selected from one of the previous builds of the same job. For more
 * complex branch source projects (i.e., projects that build several branches and pull requests in a connected job
 * hierarchy) it makes more sense to select a reference build from a job that builds the actual target branch (i.e., the
 * branch the current changes will be merged into). Here one typically is interested what changed in a branch or pull
 * request with respect to the main branch (or any other target branch): e.g., how will the code coverage change if the
 * team merges the changes. Selecting the correct reference build is not that easy, since the main branch of a project
 * will evolve more frequently than a specific feature or bugfix branch.
 * </p>
 *
 * <p>
 * This recorder unifies the computation of the reference build so consuming plugins can simply use the resulting
 * {@link ReferenceBuild} in order to get a reference for their delta reports.
 * </p>
 *
 * @author Arne Schöntag
 * @author Ullrich Hafner
 */
public class SimpleReferenceRecorder extends Recorder implements SimpleBuildStep {
    private final JenkinsFacade jenkins;
    private String referenceJob = StringUtils.EMPTY;
    private Result requiredResult = Result.UNSTABLE; // @since 2.4.0
    private boolean considerRunningBuild;

    /**
     * Creates a new instance of {@link SimpleReferenceRecorder}.
     */
    @DataBoundConstructor
    public SimpleReferenceRecorder() {
        this(new JenkinsFacade());
    }

    /**
     * Creates a new instance of {@link ReferenceRecorder}.
     *
     * @param jenkins
     *         facade to Jenkins
     */
    @VisibleForTesting
    protected SimpleReferenceRecorder(final JenkinsFacade jenkins) {
        super();

        this.jenkins = jenkins;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        if (requiredResult == null) {
            requiredResult = Result.UNSTABLE;
        }
        return this;
    }

    /**
     * Sets the reference job: this job will be used as a baseline to search for the best matching reference build. If
     * the reference job should be computed automatically (supported by {@link MultiBranchProject multi-branch projects}
     * only), then let this field empty. If the reference job is not defined and cannot be computed automatically, then
     * the current job will be used.
     *
     * @param referenceJob
     *         the name of reference job
     */
    @DataBoundSetter
    public void setReferenceJob(final String referenceJob) {
        this.referenceJob = StringUtils.strip(referenceJob);
    }

    /**
     * Returns the name of the reference job.
     *
     * @return the name of reference job
     */
    public String getReferenceJob() {
        return referenceJob;
    }

    /**
     * Sets that required build result of the reference build. If the reference build has a worse status, then the
     * selected reference build will not be used.
     *
     * @param requiredBuildResult
     *         the minimum required build result
     */
    @DataBoundSetter
    public void setRequiredBuildResult(final String requiredBuildResult) {
        this.requiredResult = Result.fromString(requiredBuildResult);
    }

    public String getRequiredBuildResult() {
        return requiredResult != null ? requiredResult.toString() : StringUtils.EMPTY;
    }

    /**
     * Sets that required build result of the reference build. If the reference build has a worse status, then the
     * selected reference build will not be used.
     *
     * @param requiredResult
     *         the minimum required build result
     */
    @DataBoundSetter
    public void setRequiredResult(final Result requiredResult) {
        this.requiredResult = requiredResult;
    }

    public Result getRequiredResult() {
        return requiredResult;
    }

    /**
     * Returns whether the specified build has a result that is better or equal than the required result.
     *
     * @param referenceBuild
     *         the reference build to check
     *
     * @return {@code true} if the build has a result that is better or equal than the required result, {@code false}
     *         otherwise
     */
    protected boolean hasRequiredResult(final Run<?, ?> referenceBuild) {
        var result = referenceBuild.getResult();

        return result != null && result.isBetterOrEqualTo(requiredResult);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public SimpleReferenceRecorderDescriptor getDescriptor() {
        return (SimpleReferenceRecorderDescriptor) super.getDescriptor();
    }

    @Override
    public void perform(@NonNull final Run<?, ?> run, @NonNull final FilePath workspace, @NonNull final EnvVars env,
            @NonNull final Launcher launcher, @NonNull final TaskListener listener) {
        var log = new FilteredLog("Errors while computing the reference build:");

        var existing = run.removeActions(ReferenceBuild.class);
        if (existing) {
            log.logError("Replaced existing reference build, this typically indicates a misconfiguration "
                    + "as the reference should be constant");
        }
        run.addAction(findReferenceBuild(run, log));

        var logHandler = new LogHandler(listener, "ReferenceFinder");
        logHandler.log(log);
    }

    /**
     * If enabled, then running builds will be considered as reference build as well. Otherwise, only completed builds
     * are considered. Enabling this option might cause problems if the reference build has not yet all the
     * required results available.
     *
     * @param considerRunningBuild
     *         if {@code true} then running builds will be considered as reference build as well
     */
    @DataBoundSetter
    public void setConsiderRunningBuild(final boolean considerRunningBuild) {
        this.considerRunningBuild = considerRunningBuild;
    }

    public boolean isConsiderRunningBuild() {
        return considerRunningBuild;
    }

    protected ReferenceBuild findReferenceBuild(final Run<?, ?> run, final FilteredLog log) {
        var reference = resolveReferenceJob(log).orElseGet(() -> fallBackToCurrentJob(run, log));
        var possibleLastCompletedBuild = getLastBuild(reference);
        if (possibleLastCompletedBuild.isEmpty()) {
            logNoBuildFound(reference, log);

            return createEmptyReferenceBuild(run, log);
        }
        else {
            var lastBuild = possibleLastCompletedBuild.get();
            log.logInfo("Found last completed build '%s' of reference job '%s'",
                    lastBuild.getDisplayName(), reference.getDisplayName());

            return getReferenceBuildWithRequiredStatus(run, lastBuild, log)
                    .orElse(createEmptyReferenceBuild(run, log));
        }
    }

    protected void logNoBuildFound(final Job<?, ?> reference, final FilteredLog log) {
        if (isConsiderRunningBuild()) {
            log.logInfo("No build found for reference job '%s'", reference.getDisplayName());
        }
        else {
            log.logInfo("No completed build found for reference job '%s'", reference.getDisplayName());
        }
    }

    /**
     * Returns a reference build that satisfied the required status. Starting with the specified {@code start} build,
     * the history of builds is searched for a build that satisfies the required status. If no such build is found, then
     * a null object is returned.
     *
     * @param run
     *         the run that is currently built
     * @param start
     *         the first build to start the search
     * @param log
     *         the logger
     *
     * @return the reference build that satisfies the required status (or empty if no such build is found)
     */
    protected Optional<ReferenceBuild> getReferenceBuildWithRequiredStatus(final Run<?, ?> run, final Run<?, ?> start, final FilteredLog log) {
        for (Run<?, ?> reference = start; reference != null; reference = reference.getPreviousCompletedBuild()) {
            if (hasRequiredResult(reference)) {
                log.logInfo("-> %s '%s' has a result %s",
                        getBuildName(start, reference),
                        reference.getDisplayName(), reference.getResult());

                return Optional.of(new ReferenceBuild(run, log.getInfoMessages(), requiredResult, reference));
            }
        }
        log.logInfo("-> ignoring reference build '%s' or one of its predecessors since none have a result of %s or better",
                start.getDisplayName(), requiredResult);
        return Optional.empty();
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private String getBuildName(final Run<?, ?> start, final Run<?, ?> reference) {
        return reference == start ? "Build" : "Previous build";
    }

    private Job<?, ?> fallBackToCurrentJob(final Run<?, ?> run, final FilteredLog log) {
        Job<?, ?> parent = run.getParent();
        log.logInfo("Falling back to current job '%s'", parent.getDisplayName());
        return parent;
    }

    protected ReferenceBuild createEmptyReferenceBuild(final Run<?, ?> run, final FilteredLog messages) {
        return new ReferenceBuild(run, messages.getInfoMessages(), requiredResult);
    }

    /**
     * Finds a reference job that should be used as a starting point to find the reference build.
     *
     * @param log
     *         the logger
     *
     * @return the reference job (if available)
     */
    protected Optional<Job<?, ?>> resolveReferenceJob(final FilteredLog log) {
        var jobName = getReferenceJob();
        if (isValidJobName(jobName)) {
            log.logInfo("Configured reference job: '%s'", jobName);

            return findJob(jobName, log);
        }

        log.logInfo("No reference job configured");
        return Optional.empty();
    }

    /**
     * Finds a job with the given name.
     *
     * @param jobName
     *         the name of the job
     * @param log
     *         the logger
     *
     * @return the job with the given name (if existing)
     */
    protected Optional<Job<?, ?>> findJob(final String jobName, final FilteredLog log) {
        Optional<Job<?, ?>> job = jenkins.getJob(jobName);
        if (job.isEmpty()) {
            log.logInfo("There is no such job - maybe the job has been renamed or deleted?");
        }
        return job;
    }

    private boolean isValidJobName(final String name) {
        return StringUtils.isNotBlank(name);
    }

    /**
     * Returns the last build of the specified reference job. If the reference build is not completed and the
     * option {@code considerRunningBuild} is set, then the last running build is returned. Otherwise, the last
     * completed build is returned.
     *
     * @param reference the reference job
     * @return the last build of the reference job
     */
    protected Optional<Run<?, ?>> getLastBuild(final Job<?, ?> reference) {
        if (isConsiderRunningBuild()) {
            return Optional.ofNullable(reference.getLastBuild());
        }
        return Optional.ofNullable(reference.getLastCompletedBuild());
    }

    /**
     * Descriptor for this step: defines the context and the UI data binding and validation.
     */
    @Extension
    @Symbol("discoverReferenceBuild")
    public static class SimpleReferenceRecorderDescriptor extends BuildStepDescriptor<Publisher> {
        private final JenkinsFacade jenkins;
        private final ReferenceJobModelValidation model;

        /**
         * Creates a new descriptor with the concrete services.
         */
        public SimpleReferenceRecorderDescriptor() {
            this(new JenkinsFacade(), new ReferenceJobModelValidation());
        }

        @VisibleForTesting
        protected SimpleReferenceRecorderDescriptor(final JenkinsFacade jenkins) {
            this(jenkins, new ReferenceJobModelValidation(jenkins));
        }

        @VisibleForTesting
        SimpleReferenceRecorderDescriptor(final JenkinsFacade jenkins, final ReferenceJobModelValidation model) {
            super();

            this.jenkins = jenkins;
            this.model =  model;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Recorder_DisplayName();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Returns the model with the possible reference jobs.
         *
         * @param project
         *         the project that is configured
         *
         * @return the model with the possible reference jobs
         */
        @POST
        public ComboBoxModel doFillReferenceJobItems(@AncestorInPath final BuildableItem project) {
            if (jenkins.hasPermission(Item.CONFIGURE, project)) {
                return model.getAllJobs();
            }
            return new ComboBoxModel();
        }

        /**
         * Performs on-the-fly validation of the reference job.
         *
         * @param project
         *         the project that is configured
         * @param referenceJob
         *         the reference job
         *
         * @return the validation result
         */
        @POST
        @SuppressWarnings("unused") // Used in jelly validation
        public FormValidation doCheckReferenceJob(@AncestorInPath final BuildableItem project,
                @QueryParameter final String referenceJob) {
            if (!jenkins.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            return model.validateJob(referenceJob);
        }

        /**
         * Returns the model with the possible build results.
         *
         * @param project
         *         the project that is configured
         *
         * @return the model with the possible build results
         */
        @POST
        public ListBoxModel doFillRequiredResultItems(@AncestorInPath final BuildableItem project) {
            var resultModel = new ListBoxModel();
            if (jenkins.hasPermission(Item.CONFIGURE, project)) {
                resultModel.add(Messages.RequiredResult_Failure(), Result.FAILURE.toString());
                resultModel.add(Messages.RequiredResult_Unstable(), Result.UNSTABLE.toString());
                resultModel.add(Messages.RequiredResult_Success(), Result.SUCCESS.toString());
            }
            return resultModel;
        }
    }
}
