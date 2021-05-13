package io.jenkins.plugins.forensics.miner;

import java.util.Optional;
import java.util.function.Predicate;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;

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
abstract class AbstractForensicsAction extends AsyncConfigurableTrendJobAction<ForensicsBuildAction> {
    private final String scmKey;

    AbstractForensicsAction(final Job<?, ?> owner, final String scmKey) {
        super(owner, ForensicsBuildAction.class);

        this.scmKey = scmKey;
    }

    public String getScmKey() {
        return scmKey;
    }

    @Override
    protected Iterable<? extends BuildResult<ForensicsBuildAction>> createBuildHistory() {
        return () -> {
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
        };
    }

    @Override
    protected LinesChartModel createChartModel(final String configuration) {
        return createChart(createBuildHistory(), ChartModelConfiguration.fromJson(configuration));
    }

    abstract LinesChartModel createChart(Iterable<? extends BuildResult<ForensicsBuildAction>> buildHistory,
            ChartModelConfiguration configuration);
}
