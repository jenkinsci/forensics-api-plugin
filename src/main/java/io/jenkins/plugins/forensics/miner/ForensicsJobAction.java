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
 * @author Ullrich Hafner
 */
public class ForensicsJobAction extends AsyncTrendJobAction<ForensicsBuildAction> {
    static final String SMALL_ICON = "/plugin/forensics-api/icons/forensics-24x24.png";
    static final String FORENSICS_ID = "forensics";

    private final String scmKey;

    /**
     * Creates a new instance of {@link ForensicsJobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @deprecated use {@link #ForensicsJobAction(Job, String)}
     */
    @Deprecated
    public ForensicsJobAction(final Job<?, ?> owner) {
        this(owner, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link ForensicsJobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param scmKey
     *         key of the repository
     */
    public ForensicsJobAction(final Job<?, ?> owner, final String scmKey) {
        super(owner, ForensicsBuildAction.class);

        this.scmKey = scmKey;
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
    @CheckForNull
    public String getIconFileName() {
        return SMALL_ICON;
    }

    @Override
    public String getUrlName() {
        return FORENSICS_ID;
    }

    @Override
    protected LinesChartModel createChartModel() {
        return new FilesCountTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
    }

    @Override
    protected Iterable<? extends BuildResult<ForensicsBuildAction>> createBuildHistory() {
        return () -> new BuildActionIterator<>(ForensicsBuildAction.class, getLatestAction(),
                a -> scmKey.equals(a.getScmKey()));
    }
}
