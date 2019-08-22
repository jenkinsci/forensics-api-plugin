package io.jenkins.plugins.forensics.reference;

import hudson.model.Run;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class ReferenceRecorder extends Recorder {

    protected static final String NO_REFERENCE_JOB = "-";

    protected Run<?, ?> run;

    protected String referenceJobName;

    protected int maxCommits;

    // Needed to register in Jenkins?
    protected String id;
    protected String name;

    public ReferenceRecorder() {
        super();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Sets the reference job to get the results for the issue difference computation.
     *
     * @param referenceJobName
     *         the name of reference job
     */
    @DataBoundSetter
    public void setReferenceJobName(final String referenceJobName) {
        if (NO_REFERENCE_JOB.equals(referenceJobName)) {
            this.referenceJobName = StringUtils.EMPTY;
        }
        this.referenceJobName = referenceJobName;
    }

    /**
     * Returns the reference job to get the results for the issue difference computation. If the job is not defined,
     * then {@link #NO_REFERENCE_JOB} is returned.
     *
     * @return the name of reference job, or {@link #NO_REFERENCE_JOB} if undefined
     */
    public String getReferenceJobName() {
        if (StringUtils.isBlank(referenceJobName)) {
            return NO_REFERENCE_JOB;
        }
        return referenceJobName;
    }

    public int getMaxCommits() {
        return maxCommits;
    }

    @DataBoundSetter
    public void setMaxCommits(final int maxCommits) {
        this.maxCommits = maxCommits;
    }

    public String getId() {
        return id;
    }

    @DataBoundSetter
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

}
