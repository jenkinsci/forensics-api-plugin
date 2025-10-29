package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.SeriesBuilder;

import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.forensics.miner.ResultStubs.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

class AddedVersusDeletedLinesTrendChartTest {
    @Test
    void shouldCreate() {
        var chart = new AddedVersusDeletedLinesTrendChart();

        var configuration = new ChartModelConfiguration();

        List<BuildResult<ForensicsBuildAction>> results = new ArrayList<>();
        results.add(createResult(1, 30));
        results.add(createResult(2, 20));

        var model = chart.create(results, configuration, createSeriesBuilderStub(configuration, results));

        assertThatJson(model)
                .node("series")
                .isArray().hasSize(2);

        assertThatJson(model).node("series[0].name").isEqualTo("Added Lines");
        assertThatJson(model).node("series[1].name").isEqualTo("Deleted Lines");
    }

    private SeriesBuilder<ForensicsBuildAction> createSeriesBuilderStub(final ChartModelConfiguration configuration,
            final List<BuildResult<ForensicsBuildAction>> results) {
        SeriesBuilder<ForensicsBuildAction> seriesBuilderStub = createSeriesBuilder();
        LinesDataSet linesDataSet = mock(LinesDataSet.class);
        when(seriesBuilderStub.createDataSet(configuration, results)).thenReturn(linesDataSet);

        return seriesBuilderStub;
    }

    @SuppressWarnings("unchecked")
    private SeriesBuilder<ForensicsBuildAction> createSeriesBuilder() {
        return mock(SeriesBuilder.class);
    }
}
