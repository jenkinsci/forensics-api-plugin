package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Job;

import io.jenkins.plugins.echarts.AsyncTrendJobAction;
import io.jenkins.plugins.echarts.BuildActionIterator;

/**
 * A job action displays a link on the side panel of a job that refers to the last build that contains forensic results
 * (i.e. a {@link ForensicsBuildAction} with a {@link RepositoryStatistics} instance). This action also is responsible
 * to render the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Giulia Del Bravo
 */
public class ForensicsCodeMetricAction extends AsyncTrendJobAction<ForensicsBuildAction> {
    private final String scmKey;

    /**
     * Creates a new instance of {@link ForensicsCodeMetricAction}.
     *
     * @param owner
     *         the job that owns this action
     * @deprecated use {@link #ForensicsCodeMetricAction(Job, String)}
     */
    @Deprecated
    public ForensicsCodeMetricAction(final Job<?, ?> owner) {
        this(owner, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link ForensicsCodeMetricAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param scmKey
     *         key of the repository
     */
    public ForensicsCodeMetricAction(final Job<?, ?> owner, final String scmKey) {
        super(owner, ForensicsBuildAction.class);

        this.scmKey = scmKey;
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

    @Override
    protected Iterable<? extends BuildResult<ForensicsBuildAction>> createBuildHistory() {
        return () -> new BuildActionIterator<>(ForensicsBuildAction.class, getLatestAction(),
                a -> scmKey.equals(a.getScmKey()));
    }
}
