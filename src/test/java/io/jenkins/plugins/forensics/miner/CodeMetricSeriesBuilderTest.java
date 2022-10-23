package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeMetricSeriesBuilderTest {

    @Test
    void shouldComputeRelativeCountStatistics() {
        final int totalLinesOfCode = 10;
        final int totalChurn = 20;
        ForensicsBuildAction forensicsBuildAction = mock(ForensicsBuildAction.class);
        when(forensicsBuildAction.getTotalLinesOfCode()).thenReturn(totalLinesOfCode);
        when(forensicsBuildAction.getTotalChurn()).thenReturn(totalChurn);

        CodeMetricSeriesBuilder codeMetricSeriesBuilder = new CodeMetricSeriesBuilder();
        Map<String,Integer> computedSeries = codeMetricSeriesBuilder.computeSeries(forensicsBuildAction);

        assertThat(computedSeries).hasSize(2);
        assertThat(computedSeries.get(CodeMetricSeriesBuilder.LOC_KEY)).isEqualTo(totalLinesOfCode);
        assertThat(computedSeries.get(CodeMetricSeriesBuilder.CHURN_KEY)).isEqualTo(totalChurn);
    }

}