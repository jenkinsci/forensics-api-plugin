package io.jenkins.plugins.forensics.miner;

import java.util.Collections;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

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
    /**
     * Creates a new instance of {@link  RepositoryMinerStep}.
     */
    @DataBoundConstructor
    public RepositoryMinerStep() {
        super();

        // empty constructor required for Stapler
    }

    @Override
    public void perform(@NonNull final Run<?, ?> run, @NonNull final FilePath workspace,
            @NonNull final Launcher launcher, @NonNull final TaskListener listener) throws InterruptedException {
        LogHandler logHandler = new LogHandler(listener, "Forensics");

        FilteredLog logger = new FilteredLog("Errors while mining source control repository:");
        logger.logInfo("Creating SCM miner to obtain statistics for affected repository files");
        RepositoryMiner miner = MinerFactory.findMiner(run, Collections.singleton(workspace), listener, logger);
        logHandler.log(logger);

        // TODO: repository mining should be an incremental process
        RepositoryStatistics repositoryStatistics = miner.mine(Collections.emptyList(), logger);
        logHandler.log(logger);

        run.addAction(new ForensicsBuildAction(run, repositoryStatistics));
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    @Symbol("mineRepository")
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Mine SCM repository";
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
