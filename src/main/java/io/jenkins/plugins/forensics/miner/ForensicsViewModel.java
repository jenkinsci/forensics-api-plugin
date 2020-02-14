package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.JacksonFacade;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;

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
    ForensicsViewModel(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics) {
        super();

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

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the severities.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getAuthorsModel() {
        return new JacksonFacade().toJson(new SizePieChart().create(repositoryStatistics,
                FileStatistics::getNumberOfAuthors, 5, 10, 15, 25, 50));
    }

    /**
     * Returns the UI model for an ECharts doughnut chart that shows the severities.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getCommitsModel() {
        return new JacksonFacade().toJson(new SizePieChart().create(repositoryStatistics,
                FileStatistics::getNumberOfCommits, 5, 10, 25, 50, 100, 250));
    }
}
