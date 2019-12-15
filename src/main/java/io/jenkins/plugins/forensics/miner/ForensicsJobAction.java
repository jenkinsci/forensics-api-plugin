package io.jenkins.plugins.forensics.miner;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Job;

import io.jenkins.plugins.echarts.BuildResult;
import io.jenkins.plugins.echarts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.JacksonFacade;
import io.jenkins.plugins.echarts.LinesChartModel;
import io.jenkins.plugins.util.JobAction;

/**
 * A job action displays a link on the side panel of a job that refers to the last build that contains forensic results
 * (i.e. a {@link ForensicsBuildAction} with a {@link RepositoryStatistics} instance). This action also is responsible
 * to render the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class ForensicsJobAction extends JobAction<ForensicsBuildAction> {
    static final String SMALL_ICON = "/plugin/forensics-api/icons/forensics-24x24.png";
    static final String FORENSICS_ID = "forensics";

    /**
     * Creates a new instance of {@link ForensicsJobAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public ForensicsJobAction(final Job<?, ?> owner) {
        super(owner, ForensicsBuildAction.class);
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
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
        return () -> new BuildResultIterator<>(getBuildActionClass(), getLatestAction());
    }
}
