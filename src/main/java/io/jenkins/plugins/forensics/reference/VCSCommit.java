package io.jenkins.plugins.forensics.reference;

import jenkins.model.RunAction2;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an Item in the List of Commits which will be used to find an Intersection with another job.
 *
 * @author Arne Schöntag
 */
@SuppressWarnings("unused")
public interface VCSCommit extends RunAction2, Serializable {
    void addGitCommitLogs(List<String> revisions);

    String getSummary();
}
