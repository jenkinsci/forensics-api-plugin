package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Job;

import io.jenkins.plugins.echarts.AsyncTrendJobAction;

public class ForensicsChurnAction extends AsyncTrendJobAction<ForensicsBuildAction> {
    static final String FORENSICS_ID = "forensics";
    static final String SMALL_ICON = "/plugin/forensics-api/icons/forensics-24x24.png";

    public ForensicsChurnAction(final Job<?, ?> owner) {
        super(owner, ForensicsBuildAction.class);
    }

    @Override
    protected LinesChartModel createChartModel() {
        return new ChurnTrendChart().create(createBuildHistory(), new ChartModelConfiguration());
    }

    @Override
    @CheckForNull
    public String getIconFileName() {
        return SMALL_ICON;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
    }

    @Override
    public String getUrlName() {
        return FORENSICS_ID;
    }
}