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
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddedVersusDeletedLinesTrendChartTest {
    @Test
    void shouldCreate() {
        AddedVersusDeletedLinesTrendChart builder = new AddedVersusDeletedLinesTrendChart();

        ChartModelConfiguration configuration = new ChartModelConfiguration();

        List<BuildResult<ForensicsBuildAction>> results = new ArrayList<>();
        results.add(createResult(1, 30));
        results.add(createResult(2, 20));

        LinesChartModel lineChartModel = builder.create(results, configuration, createSeriesBuilderStub(configuration, results));

        assertThat(lineChartModel.getSeries()).hasSize(2);

        assertThatJson(lineChartModel)
                .node("domainAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");
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
