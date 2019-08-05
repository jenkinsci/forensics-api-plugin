package io.jenkins.plugins.forensics.miner;

import java.lang.ref.WeakReference;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Run;
import jenkins.model.RunAction2;

/**
 * Persists the repository statistics for the current build.
 *
 * @author Ullrich Hafner
 */
public class FileStatisticsAction implements RunAction2 {
    private transient Run<?, ?> owner;

    @Nullable
    private transient WeakReference<RepositoryStatistics> statistics;

    public FileStatisticsAction(final Run<?, ?> run, final RepositoryStatistics statistics) {
        owner = run;
        this.statistics = new WeakReference<>(statistics);
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Repository Statistics";
    }

    @Override
    public String getUrlName() {
        return "miner";
    }
}
