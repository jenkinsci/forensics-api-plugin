package io.jenkins.plugins.forensics.reference;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.VisibleForTesting;

import java.util.Collection;
import java.util.Optional;

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
 * This recorder unifies the computation of the reference build so consuming plugins can simply use the resulting {@link
 * ReferenceBuild} in order to get a reference for their delta reports.
 * </p>
 *
 * @author Arne Schöntag
 * @author Ullrich Hafner
 */
public abstract class ReferenceRecorder extends SimpleReferenceRecorder {
    private static final String DEFAULT_TARGET_BRANCH = "master";

    private String defaultBranch = StringUtils.EMPTY;
    private boolean latestBuildIfNotFound;
    private String scm = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link ReferenceRecorder}.
     */
    protected ReferenceRecorder() {
        super();
    }

    /**
     * Creates a new instance of {@link ReferenceRecorder}.
     *
     * @param jenkins
     *         facade to Jenkins
     */
    @VisibleForTesting
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
        var actualReferenceJob = findReferenceJob(run, logger);
        if (actualReferenceJob.isPresent()) {
            var reference = actualReferenceJob.get();
            var lastBuild = getLastBuild(reference);
            if (lastBuild.isEmpty()) {
                logNoBuildFound(reference, logger);
            }
            else {
                var referenceBuild = searchForReferenceBuildWithRequiredStatus(run, lastBuild.get(), logger);
                if (referenceBuild.isPresent()) {
                    return referenceBuild.get();
                }
            }
        }
        return createEmptyReferenceBuild(run, logger);
    }

    private Optional<ReferenceBuild> searchForReferenceBuildWithRequiredStatus(final Run<?, ?> run,
            final Run<?, ?> lastCompletedBuild, final FilteredLog logger) {
        var referenceBuild = find(run, lastCompletedBuild, logger);
        if (referenceBuild.isPresent()) {
            var result = referenceBuild.get();
            logger.logInfo("Found reference build '%s' for target branch", result.getDisplayName());

            var referenceBuildWithRequiredStatus = getReferenceBuildWithRequiredStatus(run, result, logger);
            if (referenceBuildWithRequiredStatus.isPresent()) {
                return referenceBuildWithRequiredStatus;
            }
        }
        logger.logInfo("No reference build with required status found that contains matching commits");
        if (isLatestBuildIfNotFound()) {
            logger.logInfo("Falling back to latest completed build of reference job: '%s'",
                    lastCompletedBuild.getDisplayName());

            return Optional.of(new ReferenceBuild(run, logger.getInfoMessages(), getRequiredResult(), lastCompletedBuild));
        }
        return Optional.empty();
    }

    private Optional<Job<?, ?>> findReferenceJob(final Run<?, ?> run, final FilteredLog log) {
        var referenceJob = resolveReferenceJob(log);
        if (referenceJob.isPresent()) {
            return referenceJob;
        }

        return discoverJobFromMultiBranchPipeline(run, log);
    }

    @SuppressWarnings("rawtypes")
    private Optional<Job<?, ?>> discoverJobFromMultiBranchPipeline(final Run<?, ?> run, final FilteredLog logger) {
        var job = run.getParent();
        var topLevel = job.getParent();
        if (topLevel instanceof MultiBranchProject multiBranchProject) {
            logger.logInfo("Found a `MultiBranchProject`, trying to resolve the target branch from the configuration");

            var targetBranch = getTargetBranch();
            if (StringUtils.isNotEmpty(targetBranch)) {
                logger.logInfo("-> using target branch '%s' as configured in step", targetBranch);

                return findJobForTargetBranch(multiBranchProject, job, targetBranch, logger);
            }
            logger.logInfo("-> no target branch configured in step", targetBranch);

            var possibleHead = findTargetBranchHead(job);
            if (possibleHead.isPresent()) {
                var target = possibleHead.get();
                logger.logInfo("-> detected a pull or merge request for target branch '%s'", target.getName());

                return findJobForTargetBranch(multiBranchProject, job, target.getName(), logger);
            }

            var possiblePrimaryBranch = findPrimaryBranch(topLevel);
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
            if (StringUtils.isEmpty(getReferenceJob())) {
                logger.logInfo("Falling back to current job '%s'", job.getDisplayName());

                return Optional.of(job);
            }
            logger.logInfo("Do not use a reference job and skip delta reporting");
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
     * build of the reference build. This default implementation does nothing.
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
    @SuppressWarnings("unused")
    protected Optional<Run<?, ?>> find(final Run<?, ?> owner, final Run<?, ?> lastCompletedBuildOfReferenceJob,
            final FilteredLog logger) {
        return Optional.empty();
    }

    static class ScmFacade {
        Optional<ChangeRequestSCMHead> findHead(final Job<?, ?> job) {
            SCMHead head = HeadByItem.findHead(job);
            if (head instanceof ChangeRequestSCMHead mHead) {
                return Optional.of(mHead);
            }
            return Optional.empty();
        }
    }
}
