package io.jenkins.plugins.forensics.reference;

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
public interface VCSCommit extends RunAction2, Serializable {
    void addGitCommitLogs(List<String> revisions);

    String getSummary();

    String getLatestRevision();

    List<String> getRevisions();

    void addRevisions(List<String> list);

    void addRevision(String revision);

    /**
     * Tries to find the reference point of the GitCommit of another build.
     * @param reference the GitCommit of the other build
     * @param maxLogs maximal amount of commits looked at.
     * @return the build Id of the reference build or Optional.empty() if none found.
     */
    Optional<String> getReferencePoint(VCSCommit reference, int maxLogs);
}
