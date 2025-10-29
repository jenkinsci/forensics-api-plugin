package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.Strings;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LinesChartModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.echarts.AsyncConfigurableTrendChart;

/**
 * This job action is responsible to render the historical trend of the commit statistics via its associated
 * 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class CommitStatisticsJobAction extends InvisibleAction implements AsyncConfigurableTrendChart {
    enum ChartType {
        DELTA, COUNT
    }

    private static final JacksonFacade JACKSON_FACADE = new JacksonFacade();

    private final String scmKey;
    private final Job<?, ?> owner;

    CommitStatisticsJobAction(final Job<?, ?> owner, final String scmKey) {
        super();

        this.owner = owner;
        this.scmKey = scmKey;
    }

    @JavaScriptMethod
    @Override
    public String getConfigurableBuildTrendModel(final String configuration) {
        return new JacksonFacade().toJson(createChartModel(configuration));
    }

    private LinesChartModel createChartModel(final String configuration) {
        ChartModelConfiguration modelConfiguration = ChartModelConfiguration.fromJson(configuration);

        var chart = getChart(configuration);

        Iterable<? extends BuildResult<CommitStatisticsBuildAction>> buildHistory
                = createBuildHistory(modelConfiguration.getBuildCount());
        if (chart == ChartType.DELTA) {
            return new AddedVersusDeletedLinesTrendChart().create(buildHistory, modelConfiguration,
                    new AddedVersusDeletedLinesCommitStatisticsSeriesBuilder());
        }
        return new RelativeCountTrendChart().create(buildHistory, modelConfiguration,
                new RelativeCountCommitStatisticsSeriesBuilder());
    }

    private ChartType getChart(final String configuration) {
        var type = JACKSON_FACADE.getString(configuration, "chartType", "delta");
        for (ChartType chartType : ChartType.values()) {
            if (Strings.CI.equals(type, chartType.name())) {
                return chartType;
            }
        }

        return ChartType.DELTA;
    }

    private Iterable<? extends BuildResult<CommitStatisticsBuildAction>> createBuildHistory(final int buildCount) {
        List<BuildResult<CommitStatisticsBuildAction>> history = new ArrayList<>();
        for (Run<?, ?> run = owner.getLastCompletedBuild(); run != null; run = run.getPreviousBuild()) {
            Optional<CommitStatisticsBuildAction> latestAction = run.getActions(CommitStatisticsBuildAction.class)
                    .stream()
                    .filter(a -> scmKey.equals(a.getScmKey()))
                    .findAny();
            if (latestAction.isPresent()) {
                int buildTimeInSeconds = (int) (run.getTimeInMillis() / 1000);
                var build = new Build(run.getNumber(), run.getDisplayName(), buildTimeInSeconds);

                history.add(new BuildResult<>(build, latestAction.get()));
            }

            if (buildCount > 0 && history.size() >= buildCount) {
                break;
            }
        }

        return history;
    }

    @Override
    public boolean isTrendVisible() {
        Iterable<? extends BuildResult<CommitStatisticsBuildAction>> results = createBuildHistory(2);
        Iterator<? extends BuildResult<CommitStatisticsBuildAction>> iterator = results.iterator();

        if (iterator.hasNext()) {
            iterator.next();
        }
        return iterator.hasNext();
    }
}
