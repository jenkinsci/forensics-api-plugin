package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Action;
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
public class BuildAction implements LastBuildAction, RunAction2, StaplerProxy, Serializable {
    private static final long serialVersionUID = -2074456133028895573L;

    private transient Run<?, ?> owner;
    private transient ReentrantLock lock = new ReentrantLock();

    @Nullable
    private transient WeakReference<RepositoryStatistics> repositoryStatistics;

    /**
     * Creates a new instance of {@link BuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     */
    public BuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics) {
        this(owner, repositoryStatistics, true);
    }

    /**
     * Creates a new instance of {@link BuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     */
    @VisibleForTesting
    BuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics, final boolean canSerialize) {
        this.owner = owner;
        this.repositoryStatistics = new WeakReference<>(repositoryStatistics);

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

    /**
     * Returns the repository statistics. Since the object requires some amount of memory, it is stored in a {@link
     * WeakReference}. So if the current instance has been destroyed by the garbage collector then a new instance will
     * be automatically created by reading the persisted XML data from Jenkins build folder.
     *
     * @return the statistics
     */
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
        RepositoryStatistics statistics = new RepositoryStatisticsXmlStream().read(getResultXmlPath());
        repositoryStatistics = new WeakReference<>(statistics);
        return statistics;
    }

    private Path getResultXmlPath() {
        return owner.getRootDir().toPath().resolve("repository-statistics.xml");
    }

    @Override
    public String getIconFileName() {
        return JobAction.SMALL_ICON;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
    }

    /**
     * Returns the detail view for issues for all Stapler requests.
     *
     * @return the detail view for issues
     */
    @Override
    public Object getTarget() {
        return new ForensicsViewModel(owner, getRepositoryStatistics());
    }

    @Override
    public String getUrlName() {
        return JobAction.FORENSICS_ID;
    }

    /**
     * Returns a {@link BuildAction} of the specified baseline build. If there is no such action for the baseline then
     * the previous build is inspected, and so on. If no previous build contains a {@link BuildAction} then an empty
     * result is returned.
     *
     * @param baseline
     *         the baseline to start the search with
     *
     * @return the next available {@link BuildAction}, or an empty result if there is no such action
     */
    public static Optional<BuildAction> getBuildActionFromHistoryStartingFrom(@Nullable final Run<?, ?> baseline) {
        for (Run<?, ?> run = baseline; run != null; run = run.getPreviousBuild()) {
            BuildAction action = run.getAction(BuildAction.class);
            if (action != null) {
                return Optional.of(action);
            }
        }

        return Optional.empty();
    }
}
