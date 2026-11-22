package io.jenkins.plugins.forensics.util;

import hudson.scm.RepositoryBrowser;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * A {@link RepositoryBrowser} for commits. Since a {@link RepositoryBrowser} has no API to generate links to simple
 * commits, this decorator adds such a functionality. Note that this API does not only obtain such links, it also
 * renders these links as HTML "a"-tags.
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
     * @return an HTML "a"-tag that contains a link to the commit
     */
    public abstract String asLink(String id);

    /**
     * Returns the URL of the specified commit, e.g.,
     * https://github.com/jenkinsci/analysis-model/commit/9f5eb8b28e422d6249e6c29dc65173a59f2d9f6f. In contrast to
     * {@link #asLink(String)}, the URL is not rendered as HTML, so clients can use the URL in their own components.
     * The default implementation extracts the URL from the "href" attribute of the link that is returned by
     * {@link #asLink(String)}. Subclasses that know the URL should override this method.
     *
     * @param id
     *         the ID of the commit
     *
     * @return the URL of the commit, or an empty {@link Optional} if there is no URL for the commit (e.g., if the SCM
     *         has no repository browser)
     */
    public Optional<String> getUrl(final String id) {
        var url = StringUtils.substringBetween(asLink(id), "href=\"", "\"");
        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }
        return Optional.of(unescapeHtml(url));
    }

    /**
     * Returns the raw link for the specified commit ID.
     *
     * @param id
     *         the ID of the commit
     *
     * @return a raw link to the commit, e.g., https://github.com/jenkinsci/analysis-model/commit/9f5eb8b28e422d6249e6c29dc65173a59f2d9f6f,
     *         or the ID of the commit if there is no URL for the commit
     * @see #getUrl(String)
     */
    public String getRawLink(final String id) {
        return getUrl(id).orElse(id);
    }

    private static String unescapeHtml(final String value) {
        return StringUtils.replaceEach(
                value,
                new String[] {"&quot;", "&#39;", "&#x27;", "&lt;", "&gt;", "&amp;"},
                new String[] {"\"", "'", "'", "<", ">", "&"});
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

        @Override
        public Optional<String> getUrl(final String id) {
            return Optional.empty();
        }
    }
}
