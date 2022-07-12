package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.util.NoSuchElementException;

import edu.hm.hafner.echarts.JacksonFacade;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.forensics.util.CommitDecorator;
import io.jenkins.plugins.forensics.util.CommitDecoratorFactory;

/**
 * Server side model that provides the data for the details view of the repository statistics. The layout of the
 * associated view is defined corresponding jelly view 'index.jelly') in the {@link ForensicsViewModel} package.
 *
 * @author Ullrich Hafner
 */
public class ForensicsViewModel extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final RepositoryStatistics repositoryStatistics;
    private final String scmKey;

    /**
     * Creates a new {@link ForensicsViewModel} instance.
     *
     * @param owner
     *         the build as owner of this view
     * @param repositoryStatistics
     *         the statistics to show in the view
     * @param scmKey
     *         key of the repository
     */
    ForensicsViewModel(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics, final String scmKey) {
        super();

        this.owner = owner;
        this.repositoryStatistics = repositoryStatistics;
        this.scmKey = scmKey;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return Messages.Forensics_Action();
    }

    public String getScmKey() {
        return scmKey;
    }

    @Override
    public ForensicsTableModel getTableModel(final String id) {
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

    /**
     * Returns a new sub page for the selected link.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new sub page
     */
    @SuppressWarnings("unused") //called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            CommitDecorator decorator = CommitDecoratorFactory.findCommitDecorator(owner);

            return new FileDetailsView(owner, link, repositoryStatistics, decorator);
        }
        catch (NoSuchElementException nse) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException ignore) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
    }
}
