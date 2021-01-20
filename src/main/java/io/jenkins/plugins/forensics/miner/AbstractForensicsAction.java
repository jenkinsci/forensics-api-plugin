package io.jenkins.plugins.forensics.miner;

import java.util.Optional;
import java.util.function.Predicate;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;

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
abstract class AbstractForensicsAction extends AsyncTrendJobAction<ForensicsBuildAction> {
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
            Optional<ForensicsBuildAction> latestAction = getOwner().getActions(ForensicsBuildAction.class)
                    .stream()
                    .filter(predicate)
                    .findAny();

            return new BuildActionIterator<>(ForensicsBuildAction.class, latestAction, predicate);
        };
    }

    @Override
    protected LinesChartModel createChartModel() {
        return createChart(createBuildHistory(), new ChartModelConfiguration());
    }

    abstract LinesChartModel createChart(Iterable<? extends BuildResult<ForensicsBuildAction>> buildHistory,
            ChartModelConfiguration configuration);
}
