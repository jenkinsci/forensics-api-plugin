package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import javax.annotation.Nonnull;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class FileStatisticsPublisher extends Recorder implements SimpleBuildStep {
    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener) throws InterruptedException, IOException {
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
