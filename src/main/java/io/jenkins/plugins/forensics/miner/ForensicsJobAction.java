package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.Strings;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;
import java.util.function.Predicate;

import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.echarts.AsyncConfigurableTrendJobAction;
import io.jenkins.plugins.echarts.BuildActionIterator;

/**
 * A job action displays a link on the side panel of a job that refers to the last build that contains forensic results
 * (i.e. a {@link ForensicsBuildAction} with a {@link RepositoryStatistics} instance). This action also is responsible
 * to render the historical trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class ForensicsJobAction extends AsyncConfigurableTrendJobAction<ForensicsBuildAction> {
    static final String ICON = "symbol-solid/microscope plugin-font-awesome-api";
    static final String FORENSICS_ID = "forensics";
    private static final JacksonFacade JACKSON_FACADE = new JacksonFacade();

    enum ChartType {
        FILES, LOC, DELTA, COUNT
    }

    private final String scmKey;

    public String getScmKey() {
        return scmKey;
    }

    @Override
    protected Iterable<? extends BuildResult<ForensicsBuildAction>> createBuildHistory() {
        return this::createIterator;
    }

    @NonNull @VisibleForTesting
    BuildActionIterator<ForensicsBuildAction> createIterator() {
        Predicate<ForensicsBuildAction> predicate = a -> scmKey.equals(a.getScmKey());
        Run<?, ?> lastCompletedBuild = getOwner().getLastCompletedBuild();
        Optional<ForensicsBuildAction> latestAction;
        if (lastCompletedBuild == null) {
            latestAction = Optional.empty();
        }
        else {
            latestAction = lastCompletedBuild.getActions(ForensicsBuildAction.class)
                    .stream()
                    .filter(predicate)
                    .findAny();
        }
        return new BuildActionIterator<>(ForensicsBuildAction.class, latestAction, predicate);
    }

    @Override
    protected LinesChartModel createChartModel(final String configuration) {
        return createChart(createBuildHistory(), configuration);
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
        return Messages.Forensics_Action();
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
        return ICON;
    }

    @Override
    public String getUrlName() {
        return FORENSICS_ID;
    }

    LinesChartModel createChart(final Iterable<? extends BuildResult<ForensicsBuildAction>> buildHistory,
            final String configuration) {
        ChartModelConfiguration modelConfiguration = ChartModelConfiguration.fromJson(configuration);
        var chart = getChart(configuration);
        if (chart == ChartType.LOC) {
            return new CodeMetricTrendChart().create(buildHistory, modelConfiguration);
        }
        if (chart == ChartType.DELTA) {
            return new AddedVersusDeletedLinesTrendChart().create(buildHistory, modelConfiguration,
                    new AddedVersusDeletedLinesForensicsSeriesBuilder());
        }
        if (chart == ChartType.COUNT) {
            return new RelativeCountTrendChart().create(buildHistory, modelConfiguration,
                    new RelativeCountForensicsSeriesBuilder());
        }
        return new FilesCountTrendChart().create(buildHistory, modelConfiguration);
    }

    private ChartType getChart(final String configuration) {
        var type = JACKSON_FACADE.getString(configuration, "chartType", "files");
        for (ChartType chartType : ChartType.values()) {
            if (Strings.CI.equals(type, (CharSequence) chartType.name())) {
                return chartType;
            }
        }

        return ChartType.FILES;
    }
}
