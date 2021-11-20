package io.jenkins.plugins.forensics.reference;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.DataBoundSetter;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHead.HeadByItem;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;

import io.jenkins.plugins.util.JenkinsFacade;

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
public abstract class ReferenceRecorder extends SimpleReferenceRecorder {
    private static final String DEFAULT_TARGET_BRANCH = "master";

    private String defaultBranch = StringUtils.EMPTY;
    private boolean latestBuildIfNotFound = false;
    private String scm = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link ReferenceRecorder}.
     *
     * @param jenkins
     *         facade to Jenkins
     */
    protected ReferenceRecorder(final JenkinsFacade jenkins) {
        super(jenkins);
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
     *
     * @deprecated renamed to {@link #setTargetBranch(String)}
     */
    @Deprecated
    @DataBoundSetter
    public void setDefaultBranch(final String defaultBranch) {
        this.defaultBranch = StringUtils.stripToEmpty(defaultBranch);
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    /**
     * Sets the target branch for {@link MultiBranchProject multi-branch projects}: the target branch is considered the
     * base branch in your repository. The builds of all other branches and pull requests will use this target branch as
     * baseline to search for a matching reference build.
     *
     * @param targetBranch
     *         the name of the default branch
     */
    @DataBoundSetter
    public void setTargetBranch(final String targetBranch) {
        this.defaultBranch = StringUtils.stripToEmpty(targetBranch);
    }

    public String getTargetBranch() {
        return defaultBranch;
    }

    /**
     * Sets the SCM that should be used to find the reference build for. The reference recorder will select the SCM
     * based on a substring comparison, there is no need to specify the full name.
     *
     * @param scm
     *         the ID of the SCM to use (a substring of the full ID)
     */
    @DataBoundSetter
    public void setScm(final String scm) {
        this.scm = scm;
    }

    public String getScm() {
        return scm;
    }

    @VisibleForTesting
    ScmFacade getScmFacade() {
        return new ScmFacade();
    }

    @Override
    protected ReferenceBuild findReferenceBuild(final Run<?, ?> run, final FilteredLog logger) {
        Optional<Job<?, ?>> actualReferenceJob = findReferenceJob(run, logger);
        if (actualReferenceJob.isPresent()) {
            Job<?, ?> reference = actualReferenceJob.get();
            Run<?, ?> lastCompletedBuild = reference.getLastCompletedBuild();
            if (lastCompletedBuild == null) {
                logger.logInfo("No completed build found");
            }
            else {
                Optional<Run<?, ?>> referenceBuild = find(run, lastCompletedBuild, logger);
                if (referenceBuild.isPresent()) {
                    Run<?, ?> result = referenceBuild.get();
                    logger.logInfo("Found reference build '%s' for target branch", result.getDisplayName());

                    return new ReferenceBuild(run, logger.getInfoMessages(), result);
                }
                logger.logInfo("No reference build found that contains matching commits");
                if (isLatestBuildIfNotFound()) {
                    logger.logInfo("Falling back to latest build of reference job: '%s'",
                            lastCompletedBuild.getDisplayName());

                    return new ReferenceBuild(run, logger.getInfoMessages(), lastCompletedBuild);
                }
            }
        }
        return new ReferenceBuild(run, logger.getInfoMessages());
    }

    private Optional<Job<?, ?>> findReferenceJob(final Run<?, ?> run, final FilteredLog log) {
        Optional<Job<?, ?>> referenceJob = resolveReferenceJob(log);
        if (referenceJob.isPresent()) {
            return referenceJob;
        }

        return discoverJobFromMultiBranchPipeline(run, log);
    }

    @SuppressWarnings("rawtypes")
    private Optional<Job<?, ?>> discoverJobFromMultiBranchPipeline(final Run<?, ?> run, final FilteredLog logger) {
        Job<?, ?> job = run.getParent();
        ItemGroup<?> topLevel = job.getParent();
        if (topLevel instanceof MultiBranchProject) {
            MultiBranchProject<?, ?> multiBranchProject = (MultiBranchProject<?, ?>) topLevel;

            logger.logInfo("Found a `MultiBranchProject`, trying to resolve the target branch from the configuration");

            String targetBranch = getTargetBranch();
            if (StringUtils.isNotEmpty(targetBranch)) {
                logger.logInfo("-> using target branch '%s' as configured in step", targetBranch);

                return findJobForTargetBranch(multiBranchProject, job, targetBranch, logger);
            }
            logger.logInfo("-> no target branch configured in step", targetBranch);

            Optional<SCMHead> possibleHead = findTargetBranchHead(job);
            if (possibleHead.isPresent()) {
                SCMHead target = possibleHead.get();
                logger.logInfo("-> detected a pull or merge request for target branch '%s'", target.getName());

                return findJobForTargetBranch(multiBranchProject, job, target.getName(), logger);
            }

            Optional<? extends Job> possiblePrimaryBranch = findPrimaryBranch(topLevel);
            if (possiblePrimaryBranch.isPresent()) {
                Job<?, ?> primaryBranchJob = possiblePrimaryBranch.get();
                logger.logInfo("-> using configured primary branch '%s' of SCM as target branch",
                        primaryBranchJob.getDisplayName());

                return Optional.of(primaryBranchJob);
            }

            logger.logInfo("-> falling back to plugin default target branch '%s'", DEFAULT_TARGET_BRANCH);
            return findJobForTargetBranch(multiBranchProject, job, DEFAULT_TARGET_BRANCH, logger);
        }
        else {
            logger.logInfo("Consider configuring a reference job using the 'referenceJob' property");
        }
        return Optional.empty();
    }

    protected Optional<SCMHead> findTargetBranchHead(final Job<?, ?> job) {
        return getScmFacade().findHead(job).map(ChangeRequestSCMHead::getTarget);
    }

    @SuppressWarnings("rawtypes")
    private Optional<? extends Job> findPrimaryBranch(final ItemGroup<?> topLevel) {
        return topLevel.getAllItems().stream()
                .map(Item::getAllJobs)
                .flatMap(Collection::stream)
                .filter(this::hasDefaultTargetBranchDefined)
                .findAny();
    }

    private boolean hasDefaultTargetBranchDefined(final Job<?, ?> job) {
        return job.getAction(PrimaryInstanceMetadataAction.class) != null;
    }

    private Optional<Job<?, ?>> findJobForTargetBranch(final MultiBranchProject<?, ?> multiBranchProject,
            final Job<?, ?> job, final String targetBranch, final FilteredLog logger) {
        Job<?, ?> target = multiBranchProject.getItemByBranchName(targetBranch);
        if (job.equals(target)) {
            logger.logInfo("-> no reference job required - this build is already for the default target branch '%s'",
                    job.getName());
        }
        else if (target != null) {
            logger.logInfo("-> inferred job for target branch: '%s'", target.getDisplayName());

            return Optional.of(target);
        }
        logger.logError("-> no job found for target branch '%s' (is the branch correctly defined?)", targetBranch);

        return Optional.empty();
    }

    /**
     * Returns the reference build for the given build {@code owner}. The search should start with the last completed
     * build of the reference build.
     *
     * @param owner
     *         the owner to get the reference build for
     * @param lastCompletedBuildOfReferenceJob
     *         the last completed build of the reference job
     * @param logger
     *         the logger to use
     *
     * @return the reference build (if available)
     */
    protected Optional<Run<?, ?>> find(final Run<?, ?> owner, final Run<?, ?> lastCompletedBuildOfReferenceJob,
            @SuppressWarnings("unused") final FilteredLog logger) {
        return find(owner, lastCompletedBuildOfReferenceJob);
    }

    /**
     * Returns the reference build for the given build {@code owner}. The search should start with the last completed
     * build of the reference build.
     *
     * @param owner
     *         the owner to get the reference build for
     * @param lastCompletedBuildOfReferenceJob
     *         the last completed build of the reference job
     *
     * @return the reference build (if available)
     * @deprecated replaced by {@link #find(Run, Run, FilteredLog)}
     */
    @SuppressWarnings("unused")
    @Deprecated
    protected Optional<Run<?, ?>> find(final Run<?, ?> owner, final Run<?, ?> lastCompletedBuildOfReferenceJob) {
        return Optional.empty();
    }

    static class ScmFacade {
        Optional<ChangeRequestSCMHead> findHead(final Job<?, ?> job) {
            SCMHead head = HeadByItem.findHead(job);
            if (head instanceof ChangeRequestSCMHead) {
                return Optional.of((ChangeRequestSCMHead) head);
            }
            return Optional.empty();
        }
    }
}
