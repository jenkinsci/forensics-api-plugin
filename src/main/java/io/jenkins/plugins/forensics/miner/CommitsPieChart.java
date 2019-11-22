package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;

import io.jenkins.plugins.echarts.api.charts.Palette;
import io.jenkins.plugins.echarts.api.charts.PieChartModel;
import io.jenkins.plugins.echarts.api.charts.PieData;

/**
 * Builds the model for a pie chart showing the distribution of issues by severity.
 *
 * @author Ullrich Hafner
 */
public class CommitsPieChart {
    private static final int[] BREAKPOINTS = {10, 25, 50, 100, 250, 500};

    /**
     * Creates the chart for the specified result.
     *
     * @param repositoryStatistics
     *         the repository statistics to render
     *
     * @return the chart model
     */
    public PieChartModel create(final RepositoryStatistics repositoryStatistics) {
        PieChartModel model = new PieChartModel("Hello Pie");
        HashMap<Integer, Integer> distribution = new HashMap<>();
        for (FileStatistics file : repositoryStatistics.getFileStatistics()) {
            distribution.merge(determineBreakpoint(file.getNumberOfAuthors(), BREAKPOINTS), 1, Integer::sum);
        }
        for (Integer key : distribution.keySet()) {
            model.add(new PieData(String.valueOf(key), distribution.get(key)), Palette.color(key));
        }
        return model;
    }

    private int determineBreakpoint(final int numberOfAuthors, final int[] breakpoints) {
        for (int breakpoint : breakpoints) {
            if (numberOfAuthors < breakpoint) {
                return breakpoint;
            }
        }
        return breakpoints.length - 1;
    }
}
