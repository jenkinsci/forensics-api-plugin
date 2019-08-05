package io.jenkins.plugins.forensics.miner;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.forensics.util.FilteredLog;

/**
 * Publishes the results of the repository miner.
 *
 * @author Ullrich Hafner
 */
public class FileStatisticsPublisher extends Recorder implements SimpleBuildStep {
    /**
     * Creates a new instance of {@link FileStatisticsPublisher}.
     */
    @DataBoundConstructor
    public FileStatisticsPublisher() {
        // required for Stapler
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)  {
        FilteredLog log = new FilteredLog(
                "Errors while creating a repository miner for build " + run.getFullDisplayName());

        RepositoryMiner miner = MinerFactory.findMinerFor(run, workspace, listener, log);

        log.getInfoMessages().forEach(listener.getLogger()::println);
        log.logSummary();
        log.getErrorMessages().forEach(listener.getLogger()::println);

        Instant start = Instant.now();
        RepositoryStatistics statistics = miner.mine();
        Instant end = Instant.now();

        int runtime = (int) (Duration.between(start, end).toMillis() / 1000);

        log(listener, "[Forensics] Analyzed history of %d files in %d seconds", statistics.size(), runtime);

        List<FileStatistics> sorted = new ArrayList<>(statistics.getFileStatistics());

        sorted.sort(Comparator.comparingInt(FileStatistics::getNumberOfCommits).reversed());
        log(listener, "[Git Forensics] File with most commits (#%d): %s",
                sorted.get(0).getNumberOfCommits(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingInt(FileStatistics::getNumberOfAuthors).reversed());
        log(listener, "[Git Forensics] File with most number of authors (#%d): %s",
                sorted.get(0).getNumberOfAuthors(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingLong(FileStatistics::getAgeInDays).reversed());
        log(listener, "[Git Forensics] Oldest file (%d days): %s",
                sorted.get(0).getAgeInDays(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingLong(FileStatistics::getLastModifiedInDays));
        log(listener, "[Git Forensics] Least recently modified file (%d days): %s",
                sorted.get(0).getLastModifiedInDays(), sorted.get(0).getFileName());

        run.addAction(new FileStatisticsAction(run, statistics));

        statistics.getInfoMessages().forEach(listener.getLogger()::println);
        statistics.logSummary();
        statistics.getErrorMessages().forEach(listener.getLogger()::println);
    }

    private void log(final TaskListener listener, final String format, final Object... args) {
        listener.getLogger().println(String.format(format, args));
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    @Symbol("recordRepositoryStatistics")
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Record Repository Statistics";
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
