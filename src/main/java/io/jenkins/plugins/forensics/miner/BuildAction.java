package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

/**
 * Controls the live cycle of the forensics results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the forensics details: these are rendered using a new view instance.
 *
 * @author Ullrich Hafner
 */
// TODO: Results are written to build.xml
public class BuildAction implements LastBuildAction, RunAction2, ModelObject, Serializable {
    private static final long serialVersionUID = -2074456133028895573L;

    private transient Run<?, ?> owner;
    private transient ReentrantLock lock = new ReentrantLock();
    /**
     * All outstanding issues: i.e. all issues, that are part of the current and reference report.
     */
    @Nullable
    private transient WeakReference<RepositoryStatistics> repositoryStatistics;
    private final int runTime;

    /**
     * Creates a new instance of {@link BuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param runTime
     *         the runtime of the repository scanner (in seconds)
     */
    public BuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics, final int runTime) {
        this(owner, repositoryStatistics, runTime, true);
    }

    /**
     * Creates a new instance of {@link BuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param runTime
     *         the runtime of the repository scanner (in seconds)
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    public BuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final int runTime, final boolean canSerialize) {
        this.owner = owner;
        this.repositoryStatistics = new WeakReference<>(repositoryStatistics);
        this.runTime = runTime;

        if (canSerialize) {
            new RepositoryStatisticsXmlStream().write(getResultXmlPath(), repositoryStatistics);
        }
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
        lock = new ReentrantLock();
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        onAttached(r);
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new JobAction(owner.getParent()));

    }

    public int getRunTime() {
        return runTime;
    }

    public RepositoryStatistics getRepositoryStatistics() {
        lock.lock();
        try {
            if (repositoryStatistics == null) {
                return readStatistics();
            }
            RepositoryStatistics result = repositoryStatistics.get();
            if (result == null) {
                return readStatistics();
            }
            return result;
        }
        finally {
            lock.unlock();
        }
    }

    private RepositoryStatistics readStatistics() {
        RepositoryStatistics statistics = new RepositoryStatisticsXmlStream().read(
                getResultXmlPath());
        repositoryStatistics = new WeakReference<>(statistics);
        return statistics;
    }

    private Path getResultXmlPath() {
        return owner.getRootDir().toPath().resolve("repository-statistics.xml");
    }

    @Override
    public String getIconFileName() {
        return "/plugin/forensics-api/icons/forensics-24x24.png";
    }

    @Override
    public String getDisplayName() {
        return "SCM Forensics";
    }

    @Override
    public String getUrlName() {
        return "forensics";
    }
}
