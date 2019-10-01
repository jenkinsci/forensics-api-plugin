package io.jenkins.plugins.forensics.reference;

import com.google.common.annotations.VisibleForTesting;
import hudson.model.Run;
import io.jenkins.plugins.forensics.util.JenkinsFacade;
import jenkins.model.RunAction2;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Represents an Item in the List of Commits which will be used to find an Intersection with another job.
 *
 * @author Arne Schöntag
 */
@SuppressWarnings("unused")
public abstract class VCSCommit implements RunAction2, Serializable {
    private static final long serialVersionUID = -5610787867605008348L;

    private static JenkinsFacade jenkinsFacade = new JenkinsFacade();

    @VisibleForTesting
    static void setJenkinsFacade(final JenkinsFacade facade) {
        jenkinsFacade = facade;
    }

    public abstract void addGitCommitLogs(List<String> revisions);

    public abstract String getSummary();

    public abstract String getLatestRevision();

    public abstract List<String> getRevisions();

    public abstract void addRevisions(List<String> list);

    public abstract void addRevision(String revision);

    /**
     * Tries to find the reference point of the GitCommit of another build.
     * @param reference the GitCommit of the other build
     * @param maxLogs maximal amount of commits looked at.
     * @return the build Id of the reference build or Optional.empty() if none found.
     */
    public abstract Optional<String> getReferencePoint(VCSCommit reference, int maxLogs);

    public static VCSCommit findVCSCommitFor(final Run<?, ?> run) {
        return run.getAction(VCSCommit.class);
    }

    private static List<VCSCommit> findAllExtensions() {
        return jenkinsFacade.getExtensionsFor(VCSCommit.class);
    }
}
