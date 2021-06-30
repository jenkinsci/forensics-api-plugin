package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import io.jenkins.plugins.forensics.reference.ReferenceBuild;

/**
 * Controls the life cycle of the commit statistics in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class CommitStatisticsBuildAction extends InvisibleAction implements LastBuildAction, RunAction2, Serializable {
    private static final long serialVersionUID = -263122257268060032L;

    @SuppressFBWarnings(value = "SE", justification = "transient field owner ist restored using a Jenkins callback")
    private transient Run<?, ?> owner;

    private final String scmKey;
    private final CommitStatistics commitStatistics;

    /**
     * Creates a new instance of {@link CommitStatisticsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param scmKey
     *         key of the repository
     * @param commitStatistics
     *         the statistics to persist with this action
     */
    public CommitStatisticsBuildAction(final Run<?, ?> owner,
            final String scmKey, final CommitStatistics commitStatistics) {
        super();

        this.owner = owner;
        this.scmKey = scmKey;
        this.commitStatistics = commitStatistics;
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    public String getScmKey() {
        return scmKey;
    }

    public CommitStatistics getCommitStatistics() {
        return commitStatistics;
    }

    @CheckForNull
    public ReferenceBuild getReferenceBuild() {
        return getOwner().getAction(ReferenceBuild.class);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", scmKey, commitStatistics);
    }

    @Override
    public void onAttached(final Run<?, ?> run) {
        owner = run;
    }

    @Override
    public void onLoad(final Run<?, ?> run) {
        owner = run;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.emptyList();
    }
}
