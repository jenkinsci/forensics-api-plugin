package io.jenkins.plugins.forensics.miner;

import io.jenkins.plugins.echarts.api.charts.Build;
import io.jenkins.plugins.echarts.api.charts.BuildResult;

import static org.mockito.Mockito.*;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class ResultStubs {
    @SuppressWarnings("unchecked")
    public static BuildResult<RepositoryStatistics> createResult(final int buildNumber,
            final int numberOfFiles) {
        RepositoryStatistics statistics = mock(RepositoryStatistics.class);
        when(statistics.size()).thenReturn(numberOfFiles);

        Build build = new Build(buildNumber, "#" + buildNumber, 0);

        BuildResult<RepositoryStatistics> buildResult = mock(BuildResult.class);
        when(buildResult.getBuild()).thenReturn(build);
        when(buildResult.getResult()).thenReturn(statistics);

        return buildResult;
    }

}
