package io.jenkins.plugins.forensics.miner;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;

import io.jenkins.plugins.echarts.Palette;
import io.jenkins.plugins.echarts.PieChartModel;
import io.jenkins.plugins.echarts.PieData;

/**
 * Builds the model for a pie chart showing the distribution of issues by severity.
 *
 * @author Ullrich Hafner
 */
class SizePieChart {
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
        Map<Integer, Integer> distribution = new TreeMap<>();
        for (FileStatistics file : repositoryStatistics.getFileStatistics()) {
            distribution.merge(determineBreakpoint(sizeMethod.apply(file), breakpoints), 1, Integer::sum);
        }
        int color = 0;
        for (Entry<Integer, Integer> entry : distribution.entrySet()) {
            model.add(new PieData("< " + entry.getKey(), entry.getValue()), Palette.color(color++));
        }
        return model;
    }

    private int determineBreakpoint(final int size, final int... breakpoints) {
        for (int breakpoint : breakpoints) {
            if (size < breakpoint) {
                return breakpoint;
            }
        }
        return breakpoints.length - 1;
    }
}
