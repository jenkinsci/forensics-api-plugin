package io.jenkins.plugins.forensics.miner;

import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;

/**
 * Server side model that provides the data for the details view of the temporal couplings. The layout of the associated
 * view is defined in the corresponding jelly view 'index.jelly' in the {@link TemporalCouplingViewModel} package.
 *
 * @author Akash Manna
 */
public class TemporalCouplingViewModel extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final RepositoryStatistics repositoryStatistics;

    /**
     * Creates a new {@link TemporalCouplingViewModel} instance.
     *
     * @param owner
     *         the build as owner of this view
     * @param repositoryStatistics
     *         the statistics that contain the temporal couplings to show in the view
     */
    TemporalCouplingViewModel(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics) {
        super();

        this.owner = owner;
        this.repositoryStatistics = repositoryStatistics;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return Messages.TemporalCoupling_Action();
    }

    /**
     * Returns the number of temporal couplings that are shown in this view.
     *
     * @return the number of couplings
     */
    public int getNumberOfCouplings() {
        return repositoryStatistics.getTemporalCouplings().size();
    }

    @Override
    public TemporalCouplingTableModel getTableModel(final String id) {
        return new TemporalCouplingTableModel(repositoryStatistics.getTemporalCouplings());
    }
}
