package io.jenkins.plugins.forensics.miner;

import java.util.HashMap;
import java.util.function.Function;

import io.jenkins.plugins.echarts.api.charts.Palette;
import io.jenkins.plugins.echarts.api.charts.PieChartModel;
import io.jenkins.plugins.echarts.api.charts.PieData;

/**
 * Builds the model for a pie chart showing the distribution of issues by severity.
 *
 * @author Ullrich Hafner
 */
public class SizePieChart {
    /**
     * Creates the chart for the specified result.
     *
     * @param repositoryStatistics
     *         the repository statistics to render
     * @param sizeMethod
     *         the method that obtains the size property
     * @param breakpoints
     *         the breakpoints to create the pie segments for
     *
     * @return the chart model
     */
    public PieChartModel create(final RepositoryStatistics repositoryStatistics,
            final Function<FileStatistics, Integer> sizeMethod, final int... breakpoints) {
        PieChartModel model = new PieChartModel();
        HashMap<Integer, Integer> distribution = new HashMap<>();
        for (FileStatistics file : repositoryStatistics.getFileStatistics()) {
            distribution.merge(determineBreakpoint(sizeMethod.apply(file), breakpoints), 1, Integer::sum);
        }
        int color = 0;
        for (Integer key : distribution.keySet()) {
            model.add(new PieData(String.valueOf(key), distribution.get(key)), Palette.color(color++));
        }
        return model;
    }

    private int determineBreakpoint(final int size, final int[] breakpoints) {
        for (int breakpoint : breakpoints) {
            if (size < breakpoint) {
                return breakpoint;
            }
        }
        return breakpoints.length - 1;
    }
}
