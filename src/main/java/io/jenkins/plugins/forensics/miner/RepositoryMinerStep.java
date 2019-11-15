package io.jenkins.plugins.forensics.miner;

import java.util.Collections;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
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

import io.jenkins.plugins.forensics.util.FilteredLog;

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
        FilteredLog log = new FilteredLog("Errors while mining source control repository:");

        // TODO: repository mining should be an incremental process
        RepositoryMiner miner = MinerFactory.findMiner(run, Collections.singleton(workspace), listener, log);
        long nano = System.nanoTime();
        RepositoryStatistics repositoryStatistics = miner.mine(Collections.emptyList());
        long seconds = 1 + (System.nanoTime() - nano) / 1_000_000_000L;
        log.logInfo("Mining of the Git repository took %d seconds", seconds);

        run.addAction(new BuildAction(run, repositoryStatistics, (int)seconds));

        log.getInfoMessages().forEach(line -> listener.getLogger().println("[Forensics] " + line));
        log.getErrorMessages().forEach(line -> listener.getLogger().println("[Forensics Error] " + line));
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
