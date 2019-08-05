package io.jenkins.plugins.forensics.miner;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
    public void perform(final @NonNull Run<?, ?> run, final @NonNull FilePath workspace,
            final @NonNull Launcher launcher, final @NonNull TaskListener listener) throws InterruptedException {
        FilteredLog log = new FilteredLog(
                "Errors while creating a repository miner for build " + run.getFullDisplayName());

        RepositoryMiner miner = MinerFactory.findMinerFor(run, workspace, listener, log);

        Consumer<String> logMessage = message -> log(listener, message);

        log.getInfoMessages().forEach(logMessage);
        log.logSummary();
        log.getErrorMessages().forEach(logMessage);

        Instant start = Instant.now();
        RepositoryStatistics statistics = miner.mine();
        Instant end = Instant.now();

        int runtime = (int) (Duration.between(start, end).toMillis() / 1000);

        log(listener, "Analyzed history of %d files in %d seconds", statistics.size(), runtime);

        statistics.getInfoMessages().forEach(logMessage);
        statistics.logSummary();
        statistics.getErrorMessages().forEach(logMessage);

        List<FileStatistics> sorted = new ArrayList<>(statistics.getFileStatistics());
        if (!sorted.isEmpty()) {
            reportResults(listener, sorted);
        }

        run.addAction(new FileStatisticsAction(run, statistics));

    }

    private void reportResults(final TaskListener listener, final List<FileStatistics> sorted) {
        sorted.sort(Comparator.comparingInt(FileStatistics::getNumberOfCommits).reversed());
        log(listener, "File with most commits (#%d): %s",
                sorted.get(0).getNumberOfCommits(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingInt(FileStatistics::getNumberOfAuthors).reversed());
        log(listener, "File with most number of authors (#%d): %s",
                sorted.get(0).getNumberOfAuthors(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingLong(FileStatistics::getAgeInDays).reversed());
        log(listener, "Oldest file (%d days): %s",
                sorted.get(0).getAgeInDays(), sorted.get(0).getFileName());

        sorted.sort(Comparator.comparingLong(FileStatistics::getLastModifiedInDays));
        log(listener, "Least recently modified file (%d days): %s",
                sorted.get(0).getLastModifiedInDays(), sorted.get(0).getFileName());
    }

    private void log(final TaskListener listener, final String format, final Object... args) {
        listener.getLogger().println("[Forensics] " + String.format(format, args));
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    @Symbol("recordRepositoryStatistics")
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @NonNull
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
