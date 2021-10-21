package io.jenkins.plugins.forensics.miner;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.PieChartModel;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

class SizePieChartTest {

    @Test
    void shouldCreate() {
        SizePieChart chart = new SizePieChart();

        RepositoryStatistics repositoryStatistics = new RepositoryStatistics();
        FileStatisticsBuilder fileStatisticsBuilder = new FileStatistics.FileStatisticsBuilder();
        FileStatistics fileStatistics = fileStatisticsBuilder.build("Test");
        repositoryStatistics.add(fileStatistics);
        Function<FileStatistics, Integer> sizeMethod = fileStatistics1 -> 5;
        int breakpoint = 3;

        PieChartModel model = chart.create(repositoryStatistics, sizeMethod, breakpoint);

        assertThat(model.getName()).isEqualTo("");

    }
}