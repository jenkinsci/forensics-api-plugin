package io.jenkins.plugins.forensics.miner;

import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.api.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.api.TableModel;

/**
 * Server side model that provides the data for the details view of the repository statistics. The layout of the
 * associated view is defined corresponding jelly view 'index.jelly') in the {@link ForensicsViewModel} package.
 *
 * @author Ullrich Hafner
 */
public class ForensicsViewModel extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final RepositoryStatistics repositoryStatistics;

    /**
     * Creates a new {@link ForensicsViewModel} instance.
     *
     * @param owner
     *         the build as owner of this view
     * @param repositoryStatistics
     *         the statistics to show in the view
     */
    public ForensicsViewModel(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics) {
        this.owner = owner;
        this.repositoryStatistics = repositoryStatistics;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
    }

    @Override
    public TableModel getTableModel(final String id) {
        return new ForensicsTableModel(repositoryStatistics);
    }
}
