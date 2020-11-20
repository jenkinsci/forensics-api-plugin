package io.jenkins.plugins.forensics.util;

import hudson.scm.RepositoryBrowser;

/**
 * A {@link RepositoryBrowser} for commits. Since a {@link RepositoryBrowser} has no API to generate links to simple
 * commits, this decorator adds such a functionality. Note that this API does not only obtain such links, it also
 * renders these links as HTML a tags.
 *
 * @author Ullrich Hafner
 */
public abstract class CommitDecorator {
    /**
     * Obtains a link for the specified commit ID.
     *
     * @param id
     *         the ID of the commit
     *
     * @return an HTML a tag that contains a link to the commit
     */
    public abstract String asLink(String id);

    /**
     * Renders the commit ID as a human readable text.
     *
     * @param id
     *         the ID of the commit
     *
     * @return a commit ID as human readable text
     */
    public String asText(final String id) {
        return id;
    }

    /**
     * A decorator that does nothing.
     */
    public static class NullDecorator extends CommitDecorator {
        @Override
        public String asLink(final String id) {
            return id;
        }
    }
}
