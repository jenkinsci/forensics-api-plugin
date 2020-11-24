package io.jenkins.plugins.forensics.reference;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;

import org.kohsuke.stapler.DataBoundSetter;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;

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
    private static final String DEFAULT_BRANCH = "master";

    private String defaultBranch = DEFAULT_BRANCH;

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

    protected Optional<Job<?, ?>> findReferenceJob(final Run<?, ?> run, final FilteredLog log) {
        Optional<Job<?, ?>> referenceJob = resolveReferenceJob(log);
        if (referenceJob.isPresent()) {
            return referenceJob;
        }

        return discoverJobFromMultiBranchPipeline(run, log);
    }

    private Optional<Job<?, ?>> discoverJobFromMultiBranchPipeline(final Run<?, ?> run, final FilteredLog log) {
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
