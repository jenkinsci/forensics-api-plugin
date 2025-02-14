package io.jenkins.plugins.forensics.miner;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

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
    @Serial
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

    /**
     * Returns whether the statistics are based on a reference build or on the previous build.
     *
     * @return {@code true} if there is a reference build defined, {@code false} otherwise
     */
    public boolean hasReferenceBuild() {
        var referenceBuildAction = getReferenceBuild();

        return referenceBuildAction != null && referenceBuildAction.hasReferenceBuild();
    }

    /**
     * Returns the reference build action if present.
     *
     * @return the action
     */
    @CheckForNull
    public ReferenceBuild getReferenceBuild() {
        return getOwner().getAction(ReferenceBuild.class);
    }

    /**
     * Returns a link that can be used in Jelly views to navigate to the reference build.
     *
     * @return the link
     */
    public String getReferenceBuildLink() {
        var build = getReferenceBuild();
        if (build == null) {
            return ReferenceBuild.NO_REFERENCE_BUILD;
        }
        return build.getReferenceLink();
    }

    @Override
    public String toString() {
        return "%s [%s]".formatted(scmKey, commitStatistics);
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
        return Set.of(new CommitStatisticsJobAction(owner.getParent(), scmKey));
    }
}
