package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Job;

import io.jenkins.plugins.echarts.AsyncTrendJobAction;

public class ForensicsLocAction extends AsyncTrendJobAction<ForensicsBuildAction> {

    protected ForensicsLocAction(final Job<?, ?> owner) {
        super(owner, ForensicsBuildAction.class);
    }

    @Override
    protected LinesChartModel createChartModel() {
        return new LinesOfCodeTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
    }

    @Nullable
    @Override
    public String getIconFileName() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getUrlName() {
        return null;
    }
}
