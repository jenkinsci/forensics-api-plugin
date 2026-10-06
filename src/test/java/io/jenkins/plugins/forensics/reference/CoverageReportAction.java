package io.jenkins.plugins.forensics.reference;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.model.Action;

/**
 * Simulates a report that is not recorded in every build (like the code coverage report of the coverage plugin). The
 * ID of the report is exposed as the URL name of this action, so that the tests can select a build by the type or by
 * the ID of the report it provides.
 *
 * @author Akash Manna
 */
class CoverageReportAction implements Action {
    private final String id;

    CoverageReportAction(final String id) {
        this.id = id;
    }

    @Override @CheckForNull
    public String getIconFileName() {
        return null;
    }

    @Override @CheckForNull
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return id;
    }

    /**
     * A subtype that verifies that the supertypes of an action are considered by the filter as well.
     *
     * @author Akash Manna
     */
    static final class MutationCoverageReportAction extends CoverageReportAction {
        MutationCoverageReportAction(final String id) {
            super(id);
        }
    }
}
