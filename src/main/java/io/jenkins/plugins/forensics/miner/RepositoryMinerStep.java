package io.jenkins.plugins.forensics.miner;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.forensics.util.ScmResolver;
import io.jenkins.plugins.util.BuildAction;
import io.jenkins.plugins.util.LogHandler;

/**
 * A pipeline {@link Step} or Freestyle or Maven {@link Recorder} that obtains statistics for all repository files. The
 * following statistics are computed:
 * <ul>
 *     <li>total number of commits</li>
 *     <li>total number of different authors</li>
 *     <li>creation time</li>
 *     <li>last modification time</li>
 * </ul>
 * Stores the created statistics in a {@link RepositoryStatistics} instance. The result is attached to
 * a {@link Run} by registering a {@link ForensicsBuildAction}.
 *
 * @author Ullrich Hafner
 */
public class RepositoryMinerStep extends Recorder implements SimpleBuildStep {
    private String scm = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link  RepositoryMinerStep}.
     */
    @DataBoundConstructor
    public RepositoryMinerStep() {
        super();

        // empty constructor required for Stapler
    }

    /**
     * Called after de-serialization to retain backward compatibility or to populate new elements (that would be
     * otherwise initialized to {@code null}).
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (scm == null) {
            scm = StringUtils.EMPTY;
        }
        return this;
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

    @Override
    public void perform(@NonNull final Run<?, ?> run, @NonNull final FilePath workspace, @NonNull final EnvVars env,
            @NonNull final Launcher launcher, @NonNull final TaskListener listener) throws InterruptedException {
        mineRepositories(run, workspace, listener);
    }

    private void mineRepositories(final Run<?, ?> run, final FilePath workspace, final TaskListener listener)
            throws InterruptedException {
        int number = 0;
        for (SCM repository : new ScmResolver().getScms(run, getScm())) {
            long startOfMining = System.nanoTime();
            LogHandler logHandler = new LogHandler(listener, "Forensics");

            FilteredLog logger = new FilteredLog("Errors while mining " + repository);
            logger.logInfo("Creating SCM miner to obtain statistics for affected repository files");
            logger.logInfo("-> checking SCM '%s'", repository.getKey());

            RepositoryMiner miner = MinerFactory.findMiner(repository, run, workspace, listener, logger);
            logHandler.log(logger);

            RepositoryStatistics repositoryStatistics = previousBuildStatistics(scm, run);
            RepositoryStatistics addedRepositoryStatistics = miner.mine(repositoryStatistics, logger);

            logHandler.log(logger);
            int miningDurationSeconds = (int) (1 + (System.nanoTime() - startOfMining) / 1_000_000_000L);
            run.addAction(new ForensicsBuildAction(run, addedRepositoryStatistics, miningDurationSeconds,
                    repository.getKey(), number++));
        }
    }

    private RepositoryStatistics previousBuildStatistics(final String repository, final Run<?, ?> run) {
        for (Run<?, ?> build = run.getPreviousBuild(); build != null; build = build.getPreviousBuild()) {
            List<ForensicsBuildAction> actions = build.getActions(ForensicsBuildAction.class);
            if (!actions.isEmpty()) {
                return actions.stream()
                        .filter(a -> a.getScmKey().contains(repository))
                        .findAny()
                        .map(BuildAction::getResult)
                        .orElse(new RepositoryStatistics());
            }
        }

        return new RepositoryStatistics();
    }

    @Override
    public Descriptor getDescriptor() {
        return (Descriptor) super.getDescriptor();
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    @Symbol("mineRepository")
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Step_Name();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
