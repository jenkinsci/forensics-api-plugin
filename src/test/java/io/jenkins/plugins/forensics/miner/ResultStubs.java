package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.util.VisibleForTesting;

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
    @VisibleForTesting
    public static BuildResult<ForensicsBuildAction> createResult(final int buildNumber,
            final int numberOfFiles) {
        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getNumberOfFiles()).thenReturn(numberOfFiles);

        Build build = new Build(buildNumber);

        return new BuildResult<>(build, action);
    }

    private ResultStubs() {
        // prevents instantiation
    }
}
