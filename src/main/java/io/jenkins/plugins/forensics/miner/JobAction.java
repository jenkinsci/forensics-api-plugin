package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

/**
 * A job action displays a link on the side panel of a job. This action also is responsible to render the historical
 * trend via its associated 'floatingBox.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class JobAction implements Action {
    private final Job<?, ?> owner;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public JobAction(final Job<?, ?> owner) {
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return "SCM Forensics";
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @Nullable
    public String getIconFileName() {
        return "/plugin/forensics-api/icons/forensics-24x24.png";
    }

    @Override
    public String getUrlName() {
        return "forensics";
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @throws IOException
     *         in case of an error
     */
    @SuppressWarnings("unused") // Called by jelly view
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        Optional<BuildAction> action = getLatestAction();
        if (action.isPresent()) {
            BuildAction buildAction = action.get();
            response.sendRedirect2(String.format("../../../%s%s",
                    buildAction.getOwner().getUrl(), buildAction.getUrlName()));
        }
    }

    /**
     * Returns the latest results for this job.
     *
     * @return the latest results (if available)
     */
    public Optional<BuildAction> getLatestAction() {
        for (Run<?, ?> run = owner.getLastBuild(); run != null; run = run.getPreviousBuild()) {
            BuildAction action = run.getAction(BuildAction.class);
            if (action != null) {
                return Optional.of(action);
            }
        }

        return Optional.empty();
    }
}
