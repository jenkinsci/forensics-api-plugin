package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.*;

class CodeMetricSeriesBuilderTest {
    @Test
    void shouldComputeRelativeCountStatistics() {
        int totalLinesOfCode = 10;
        int totalChurn = 20;

        var action = mock(ForensicsBuildAction.class);
        when(action.getTotalLinesOfCode()).thenReturn(totalLinesOfCode);
        when(action.getTotalChurn()).thenReturn(totalChurn);

        assertThat(new CodeMetricSeriesBuilder().computeSeries(action))
                .containsExactly(entry(CodeMetricSeriesBuilder.LOC_KEY, totalLinesOfCode),
                entry(CodeMetricSeriesBuilder.CHURN_KEY, totalChurn));
    }
}
