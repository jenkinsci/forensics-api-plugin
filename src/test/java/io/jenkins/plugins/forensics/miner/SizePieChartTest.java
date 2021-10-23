package io.jenkins.plugins.forensics.miner;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.PieChartModel;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SizePieChartTest {

    @Test
    void shouldCreate() {
        SizePieChart chart = new SizePieChart();

        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);

        FileStatistics fileStatistics = new FileStatisticsBuilder().build("Hi");


        int breakpoint = 3;

        PieChartModel model = chart.create(repositoryStatisticsStub, FileStatistics::getNumberOfCommits, breakpoint);

        assertThat(model.getData()).isEmpty();
    }
    @Test
    void shouldCreate2() {
        SizePieChart chart = new SizePieChart();

        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        FileStatistics fileStatistics = new FileStatisticsBuilder().build("Hi");
        HashSet<FileStatistics> hashSet = new HashSet<>();
        hashSet.add(fileStatistics);
        when(repositoryStatisticsStub.getFileStatistics()).thenReturn(hashSet);

        int breakpoint = 3;

        PieChartModel model = chart.create(repositoryStatisticsStub, FileStatistics::getNumberOfCommits, breakpoint);

        assertThat(model.getData()).isEmpty();
    }
}