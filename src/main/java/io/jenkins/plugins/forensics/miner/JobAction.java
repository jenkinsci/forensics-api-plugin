package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.echarts.api.charts.Build;
import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.JacksonFacade;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;

/**
 * A job action displays a link on the side panel of a job that refers to the last build that contains forensic results
 * (i.e. a {@link BuildAction} with a {@link RepositoryStatistics} instance). This action also is responsible to render
 * the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class JobAction implements Action {
    static final String SMALL_ICON = "/plugin/forensics-api/icons/forensics-24x24.png";
    static final String FORENSICS_ID = "forensics";

    private final Job<?, ?> owner;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public JobAction(final Job<?, ?> owner) {
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @Nullable
    public String getIconFileName() {
        return SMALL_ICON;
    }

    @Override
    public String getUrlName() {
        return FORENSICS_ID;
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @throws IOException
     *         in case of an error
     */
    @SuppressWarnings("unused") // Called by jelly view
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        Optional<BuildAction> action = getLatestAction();
        if (action.isPresent()) {
            BuildAction buildAction = action.get();
            response.sendRedirect2(String.format("../../../%s%s",
                    buildAction.getOwner().getUrl(), buildAction.getUrlName()));
        }
    }

    /**
     * Returns the latest results for this job.
     *
     * @return the latest results (if available)
     */
    public Optional<BuildAction> getLatestAction() {
        return BuildAction.getBuildActionFromHistoryStartingFrom(owner.getLastBuild());
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getBuildTrend() {
        return new JacksonFacade().toJson(createChartModel());
    }

    private LinesChartModel createChartModel() {
        return new FilesCountTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
    }

    private Iterable<? extends BuildResult<RepositoryStatistics>> createBuildHistory() {
        return (Iterable<BuildResult<RepositoryStatistics>>) () -> new RepositoryStatisticsIterator(getLatestAction());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class RepositoryStatisticsIterator implements Iterator<BuildResult<RepositoryStatistics>> {
        private Optional<BuildAction> latestAction;

        RepositoryStatisticsIterator(final Optional<BuildAction> latestAction) {
            this.latestAction = latestAction;
        }

        @Override
        public boolean hasNext() {
            return latestAction.isPresent();
        }

        @Override
        public BuildResult<RepositoryStatistics> next() {
            if (!latestAction.isPresent()) {
                throw new NoSuchElementException();
            }
            BuildAction buildAction = latestAction.get();
            Run<?, ?> run = buildAction.getOwner();
            latestAction = BuildAction.getBuildActionFromHistoryStartingFrom(run.getPreviousBuild());

            return new BuildResult<>(new Build(run), buildAction.getRepositoryStatistics());
        }
    }
}
