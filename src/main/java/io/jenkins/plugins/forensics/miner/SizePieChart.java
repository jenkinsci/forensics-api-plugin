package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;

import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the model for a pie chart showing the distribution of issues by a configurable {@code size} property of the
 * {@link FileStatistics} instances of the underlying model. E.g., this chart can show the distribution of issues by
 * number of authors or commits. The resulting pie chart groups these numbers in a set of given intervals of the form:
 * <pre>
 * {@code }
 *     [0, b_1], [(b_1 + 1), b2], [(b_2 + 1), b3], ...
 * </pre>
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
        var model = new PieChartModel();
        Map<Integer, Integer> distribution = new TreeMap<>();
        for (FileStatistics file : repositoryStatistics.getFileStatistics()) {
            distribution.merge(determineBreakpoint(sizeMethod.apply(file), breakpoints), 1, Integer::sum);
        }
        int color = 0;
        for (Entry<Integer, Integer> entry : distribution.entrySet()) {
            model.add(new PieData("< " + entry.getKey(), entry.getValue()), JenkinsPalette.chartColor(color++).normal());
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
