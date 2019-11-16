package io.jenkins.plugins.forensics.miner;

import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.api.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.api.TableModel;

public class ForensicsDetail extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final RepositoryStatistics repositoryStatistics;

    public ForensicsDetail(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics) {
        this.owner = owner;
        this.repositoryStatistics = repositoryStatistics;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return "SCM Forensics";
    }

    @Override
    public TableModel getTableModel(final String id) {
        return new ForensicsModel(repositoryStatistics);
    }
}
