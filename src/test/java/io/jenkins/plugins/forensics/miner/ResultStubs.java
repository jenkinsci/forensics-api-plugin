package io.jenkins.plugins.forensics.miner;

import io.jenkins.plugins.echarts.api.charts.Build;
import io.jenkins.plugins.echarts.api.charts.BuildResult;

import static org.mockito.Mockito.*;

/**
 * Provides some factory methods to create stubs of {@link BuildResult build results}.
 *
 * @author Ullrich Hafner
 */
public final class ResultStubs {
    /**
     * Creates a new build result for the given build.
     *
     * @param buildNumber
     *         the number of the build
     * @param numberOfFiles
     *         the number of files in the build
     *
     * @return the {@link BuildResult} stub, that contains a {@link RepositoryStatistics} instance with the specified
     *         behavior
     */
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

    private ResultStubs() {
        // prevents instantiation
    }
}
