package io.jenkins.plugins.forensics.reference;

import jenkins.model.RunAction2;

import java.io.Serializable;
import java.util.List;

public interface VCSCommit extends RunAction2, Serializable {
    public abstract void addGitCommitLogs(List<String> revisions);

    public abstract String getSummary();
}
