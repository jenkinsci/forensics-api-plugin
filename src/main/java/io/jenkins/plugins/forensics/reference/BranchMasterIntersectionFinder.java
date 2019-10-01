package io.jenkins.plugins.forensics.reference;

import jenkins.model.RunAction2;

import java.io.Serializable;
import java.util.Optional;

/**
 * This class tries to find the revision of the last shared Commit of the current VCS branch and the master branch.
 *
 * @author Arne Schöntag
 */
public abstract class BranchMasterIntersectionFinder implements RunAction2, Serializable {

    private static final long serialVersionUID = -4549516129641755356L;

    /**
     * Method to determine the Revision of the last Commit which is shared with the master branch.
     *
     * @return the hash value (ObjectId) of the revision
     *          or null if an error occurred during evaluation or no intersection was found (should not happen)
     */
    public abstract Optional<String> findReferencePoint();

    public abstract String getSummary();

    public abstract String getBuildId();

}
