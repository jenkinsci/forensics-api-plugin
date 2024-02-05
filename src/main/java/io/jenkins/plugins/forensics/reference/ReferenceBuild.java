package io.jenkins.plugins.forensics.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.RunAction2;

import io.jenkins.plugins.bootstrap5.MessagesViewModel;
import io.jenkins.plugins.util.JenkinsFacade;

import static j2html.TagCreator.*;

/**
 * Stores the reference build for a given build. The reference build is a build in a different (or same) Jenkins job
 * that can be used to compute delta reports.
 *
 * @author Ullrich Hafner
 * @see ReferenceRecorder
 */
public class ReferenceBuild implements RunAction2, Serializable, StaplerProxy {
    private static final long serialVersionUID = -4549516129641755356L;

    /**
     * Indicates that no reference build has been found. Note that this value is not used when the build has been found
     * initially but has been deleted afterward.
     */
    public static final String NO_REFERENCE_BUILD = "-";

    static final String REFERENCE_DETAILS_URL = "reference";

    /**
     * Returns a link that can be used in Jelly views to navigate to the reference build.
     *
     * @param referenceBuildId
     *         ID of the reference build
     *
     * @return the link
     */
    public static String getReferenceBuildLink(final String referenceBuildId) {
        if (!isValidBuildId(referenceBuildId)) {
            return NO_REFERENCE_BUILD;
        }
        JenkinsFacade jenkinsFacade = new JenkinsFacade();
        Optional<Run<?, ?>> possibleReferenceBuild = jenkinsFacade.getBuild(referenceBuildId);
        if (possibleReferenceBuild.isPresent()) {
            return createLink(possibleReferenceBuild.get(), jenkinsFacade);
        }
        return String.format("#%s", referenceBuildId);
    }

    private final String referenceBuildId;
    private Result requiredResult; // @since 2.4.0
    private final JenkinsFacade jenkinsFacade;
    private final List<String> messages;

    @SuppressFBWarnings(value = "SE", justification = "transient field owner ist restored using a Jenkins callback")
    private transient Run<?, ?> owner;

    /**
     * Creates a new instance of {@link ReferenceBuild} that indicates that no reference build has been found.
     *
     * @param owner
     *         the current run as the owner of this action
     * @param messages
     *         messages that show the steps the resolution process
     * @param requiredResult
     *        the required build result of the chosen reference build
     */
    public ReferenceBuild(final Run<?, ?> owner, final List<String> messages, final Result requiredResult) {
        this(owner, messages, NO_REFERENCE_BUILD, requiredResult);
    }

    /**
     * Creates a new instance of {@link ReferenceBuild} that points to the specified reference build.
     *
     * @param owner
     *         the current build as the owner of this action
     * @param messages
     *         messages that show the steps of the resolution process
     * @param requiredResult
     *         the required build result of the chosen reference build
     * @param referenceBuild
     *         the found reference build
     */
    public ReferenceBuild(final Run<?, ?> owner, final List<String> messages, final Result requiredResult,
            final Run<?, ?> referenceBuild) {
        this(owner, messages, referenceBuild.getExternalizableId(), requiredResult);
    }

    /**
     * Creates a new instance of {@link ReferenceBuild} that indicates that no reference build has been found.
     *
     * @param owner
     *         the current run as the owner of this action
     * @param messages
     *         messages that show the steps the resolution process
     * @deprecated use {@link #ReferenceBuild(Run, List, Result)} instead
     */
    @Deprecated
    public ReferenceBuild(final Run<?, ?> owner, final List<String> messages) {
        this(owner, messages, NO_REFERENCE_BUILD, Result.UNSTABLE);
    }

    /**
     * Creates a new instance of {@link ReferenceBuild} that points to the specified reference build.
     *
     * @param owner
     *         the current build as the owner of this action
     * @param messages
     *         messages that show the steps of the resolution process
     * @param referenceBuild
     *         the found reference build
     * @deprecated use {@link #ReferenceBuild(Run, List, Result, Run)} instead
     */
    @Deprecated
    public ReferenceBuild(final Run<?, ?> owner, final List<String> messages, final Run<?, ?> referenceBuild) {
        this(owner, messages, referenceBuild.getExternalizableId(), Result.UNSTABLE);
    }

    private ReferenceBuild(final Run<?, ?> owner, final List<String> messages, final String referenceBuildId, final Result requiredResult) {
        this(owner, messages, referenceBuildId, requiredResult, new JenkinsFacade());
    }

    @VisibleForTesting
    ReferenceBuild(final Run<?, ?> owner, final List<String> messages, final String referenceBuildId,
            final Result requiredResult, final JenkinsFacade jenkinsFacade) {
        this.owner = owner;
        this.messages = new ArrayList<>(messages);
        this.referenceBuildId = referenceBuildId;
        this.requiredResult = requiredResult;
        this.jenkinsFacade = jenkinsFacade;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        if (requiredResult == null) {
            requiredResult = Result.UNSTABLE;
        }
        return this;
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

    public List<String> getMessages() {
        return messages;
    }

    /**
     * Returns a link that can be used in Jelly views to navigate to the reference build.
     *
     * @return the link
     */
    public String getReferenceLink() {
        return getReferenceBuild().map(run -> createLink(run, jenkinsFacade))
                .orElse(String.format("Reference build '%s' not found anymore "
                        + "- maybe the build has been renamed or deleted?", getReferenceBuildId()));
    }

    private static String createLink(final Run<?, ?> run, final JenkinsFacade jenkinsFacade) {
        return a().withText(run.getFullDisplayName())
                .withHref(jenkinsFacade.getAbsoluteUrl(run.getUrl()))
                .withClasses("model-link", "inside").render();
    }

    /**
     * Determines if a reference build has been recorded or not.
     *
     * @return {@code true} if a reference build has been recorded, {@code false} if not
     */
    public boolean hasReferenceBuild() {
        return isValidBuildId(referenceBuildId);
    }

    private static boolean isValidBuildId(final String buildId) {
        return !StringUtils.equals(buildId, NO_REFERENCE_BUILD);
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

    /**
     * Returns the required build result of the chosen reference build.
     *
     * @return the required build result
     */
    public Result getRequiredResult() {
        return requiredResult;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return REFERENCE_DETAILS_URL;
    }

    /**
     * Returns the detail view for the messages captured during the computation of the reference build.
     *
     * @return the detail view for the messages
     */
    @Override
    public Object getTarget() {
        return new MessagesViewModel(getOwner(), Messages.Messages_DisplayName(), messages);
    }
}
