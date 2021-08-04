package io.jenkins.plugins.forensics.util;

import org.apache.commons.lang3.StringUtils;

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
     * Returns the raw link for the specified commit ID.
     *
     * @param id
     *         the ID of the commit
     *
     * @return a raw link to the commit, e.g. https://github.com/jenkinsci/analysis-model/commit/9f5eb8b28e422d6249e6c29dc65173a59f2d9f6f
     */
    public String getRawLink(final String id) {
        return StringUtils.defaultString(
                StringUtils.substringBetween(asLink(id), "href=\"", "\""),
                id);
    }

    /**
     * Renders the commit ID as a human-readable text.
     *
     * @param id
     *         the ID of the commit
     *
     * @return a commit ID as human-readable text
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
