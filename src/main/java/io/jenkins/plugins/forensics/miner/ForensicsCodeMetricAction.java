package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Job;

import io.jenkins.plugins.echarts.AsyncTrendJobAction;
/**
 * A job action displays a link on the side panel of a job that refers to the last build that contains forensic results
 * (i.e. a {@link ForensicsBuildAction} with a {@link RepositoryStatistics} instance). This action also is responsible
 * to render the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Giulia Del Bravo
 */
public class ForensicsCodeMetricAction extends AsyncTrendJobAction<ForensicsBuildAction> {

    /**
     * Creates a new instance of {@link ForensicsCodeMetricAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public ForensicsCodeMetricAction(final Job<?, ?> owner) {
        super(owner, ForensicsBuildAction.class);
    }

    @Override
    protected LinesChartModel createChartModel() {
        return new ForensicsCodeMetricTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @CheckForNull
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}