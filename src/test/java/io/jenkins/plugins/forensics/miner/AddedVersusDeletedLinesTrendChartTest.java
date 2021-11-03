package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.SeriesBuilder;

import static io.jenkins.plugins.forensics.miner.ResultStubs.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

class AddedVersusDeletedLinesTrendChartTest {
    @Test
    void shouldCreate() {
        AddedVersusDeletedLinesTrendChart chart = new AddedVersusDeletedLinesTrendChart();

        ChartModelConfiguration configuration = new ChartModelConfiguration();

        List<BuildResult<ForensicsBuildAction>> results = new ArrayList<>();
        results.add(createResult(1, 30));
        results.add(createResult(2, 20));

        LinesChartModel model = chart.create(results, configuration, createSeriesBuilderStub(configuration, results));

        assertThatJson(model)
                .node("series")
                .isArray().hasSize(2);
        
        assertThatJson(model).node("series[0].name").isEqualTo("Added Lines");
        assertThatJson(model).node("series[1].name").isEqualTo("Deleted Lines");
    }

    private SeriesBuilder createSeriesBuilderStub(final ChartModelConfiguration configuration,
            final List<BuildResult<ForensicsBuildAction>> results) {
        SeriesBuilder seriesBuilderStub = mock(SeriesBuilder.class);
        LinesDataSet linesDataSet = mock(LinesDataSet.class);
        when(seriesBuilderStub.createDataSet(configuration, results))
                .thenReturn(linesDataSet);

        return seriesBuilderStub;
    }
}
