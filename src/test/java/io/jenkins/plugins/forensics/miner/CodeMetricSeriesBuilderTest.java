package io.jenkins.plugins.forensics.miner;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CodeMetricSeriesBuilderTest {
    @Test
    void computeSeries() {

        ForensicsBuildAction action = mock(ForensicsBuildAction.class);
        when(action.getTotalLinesOfCode()).thenReturn(2);//
        when(action.getTotalChurn()).thenReturn(5);
        CodeMetricSeriesBuilder builder = new CodeMetricSeriesBuilder();
        Map<String, Integer> series = builder.computeSeries(action);
        assertThat(series.get("loc")).isEqualTo(2);
        assertThat(series.get("churn")).isEqualTo(5);
       
    }

    //Bonus: Assert ob die Klassen die Methoden haben

} 