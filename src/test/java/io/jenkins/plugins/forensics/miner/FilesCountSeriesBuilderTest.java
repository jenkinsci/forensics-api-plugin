package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.LinesDataSet;

import static io.jenkins.plugins.forensics.miner.FilesCountSeriesBuilder.*;
import static io.jenkins.plugins.forensics.miner.ResultStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link FilesCountSeriesBuilder}.
 *
 * @author Ullrich Hafner
 */
class FilesCountSeriesBuilderTest {
    @Test
    void shouldHaveEmptyDataSetForEmptyIterator() {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();

        LinesDataSet model = builder.createDataSet(createConfiguration(), new ArrayList<>());

        assertThat(model.getDomainAxisSize()).isEqualTo(0);
        assertThat(model.getDataSetIds()).isEmpty();
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }

    @Test
    void shouldHaveThreeValuesForSingleBuild() {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();

        BuildResult<ForensicsBuildAction> singleResult = createResult(1, 1);

        LinesDataSet dataSet = builder.createDataSet(createConfiguration(), Collections.singletonList(singleResult));

        assertThat(dataSet.getDomainAxisSize()).isEqualTo(1);
        assertThat(dataSet.getDomainAxisLabels()).containsExactly("#1");

        assertThat(dataSet.getDataSetIds()).containsExactlyInAnyOrder(TOTALS_KEY);

        assertThat(dataSet.getSeries(TOTALS_KEY)).containsExactly(1);
    }

    @Test
    void shouldHaveNotMoreValuesThatAllowed() {
        FilesCountSeriesBuilder builder = new FilesCountSeriesBuilder();

        ChartModelConfiguration configuration = createConfiguration();
        when(configuration.getBuildCount()).thenReturn(3);
        when(configuration.isBuildCountDefined()).thenReturn(true);

        LinesDataSet dataSet = builder.createDataSet(configuration, Arrays.asList(
                createResult(4, 4),
                createResult(3, 3),
                createResult(2, 2),
                createResult(1, 1)
        ));

        assertThat(dataSet.getDomainAxisSize()).isEqualTo(3);
        assertThat(dataSet.getDomainAxisLabels()).containsExactly("#2", "#3", "#4");

        assertThat(dataSet.getDataSetIds()).containsExactlyInAnyOrder(TOTALS_KEY);

        assertThat(dataSet.getSeries(TOTALS_KEY)).containsExactly(2, 3, 4);
    }
}
