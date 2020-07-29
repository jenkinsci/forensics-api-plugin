package io.jenkins.plugins.forensics.reference;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import hudson.model.Run;
import jenkins.model.RunAction2;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Stores the reference build for the current build. The reference build is a build in a possibly different Jenkins job
 * that can be used to compute delta reports.
 *
 * @author Ullrich Hafner
 */
public class ReferenceBuild implements RunAction2, Serializable {
    private static final long serialVersionUID = -4549516129641755356L;

    /**
     * Indicates that no reference build has been found. Note that this value is not used when the build has been found
     * initially but has been deleted afterwards.
     */
    public static final String NO_REFERENCE_BUILD = "-";

    private final String referenceBuildId;
    private final JenkinsFacade jenkinsFacade = new JenkinsFacade();

    private transient Run<?, ?> owner;

    /**
     * Creates a new instance of {@link ReferenceBuild}.
     *
     * @param owner
     *         the current build as owner of this action
     * @param referenceBuildId
     *         the ID of the reference build
     */
    protected ReferenceBuild(final Run<?, ?> owner, final String referenceBuildId) {
        this.referenceBuildId = referenceBuildId;
        this.owner = owner;
    }

    @Override
    public void onAttached(final Run<?, ?> run) {
        this.owner = run;
    }

    @Override
    public void onLoad(final Run<?, ?> run) {
        onAttached(run);
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the reference build ID as a value to be displayed in the UI. If no reference build is available, then a
     * message will be returned.
     *
     * @return the summary message
     */
    public String getSummary() {
        if (!hasReferenceBuild()) {
            return Messages.No_Reference_Build();
        }
        return referenceBuildId;
    }

    /**
     * Determines if a reference build has been recorded or not.
     *
     * @return {@code true} if a reference build has been recorded, {@code false} if not
     */
    public boolean hasReferenceBuild() {
        return !StringUtils.equals(referenceBuildId, NO_REFERENCE_BUILD);
    }

    /**
     * Returns the ID of the reference build. If no reference build is available, then the constant string {@link
     * #NO_REFERENCE_BUILD} will be returned.
     *
     * @return the ID of the reference build
     */
    public String getReferenceBuildId() {
        return referenceBuildId;
    }

    /**
     * Returns the actual reference build. Note that a reference build might be not available anymore, because it has
     * been deleted in the meantime.
     *
     * @return the reference build, if still available
     */
    public Optional<Run<?, ?>> getReferenceBuild() {
        if (hasReferenceBuild()) {
            return jenkinsFacade.getBuild(referenceBuildId);
        }
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return Messages.Action_DisplayName();
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
