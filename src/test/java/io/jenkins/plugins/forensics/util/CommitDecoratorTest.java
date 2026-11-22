package io.jenkins.plugins.forensics.util;

import static org.assertj.core.api.Assertions.*;

import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link CommitDecorator}.
 *
 * @author Ullrich Hafner
 */
class CommitDecoratorTest {
    private static final String COMMIT = "9f5eb8b28e422d6249e6c29dc65173a59f2d9f6f";
    private static final String URL = "https://github.com/jenkinsci/analysis-model/commit/" + COMMIT;

    @Test
    void shouldExtractUrlFromLink() {
        var decorator = createDecorator("<a href=\"%s\">%s</a>".formatted(URL, COMMIT));

        assertThat(decorator.getUrl(COMMIT)).contains(URL);
        assertThat(decorator.getRawLink(COMMIT)).isEqualTo(URL);
    }

    @Test
    void shouldUnescapeUrl() {
        var decorator = createDecorator("<a href=\"https://example.com/commit?id=1&amp;repo=a&quot;b\">1</a>");

        assertThat(decorator.getUrl(COMMIT)).contains("https://example.com/commit?id=1&repo=a\"b");
    }

    @Test
    void shouldReturnEmptyUrlIfThereIsNoLink() {
        assertThat(createDecorator(COMMIT).getUrl(COMMIT)).isEmpty();
        assertThat(createDecorator(COMMIT).getRawLink(COMMIT)).isEqualTo(COMMIT);
        assertThat(createDecorator("<a href=\"\">1</a>").getUrl(COMMIT)).isEmpty();
    }

    @Test
    void shouldReturnEmptyUrlForNullDecorator() {
        var decorator = new NullDecorator();

        assertThat(decorator.getUrl(COMMIT)).isEmpty();
        assertThat(decorator.getRawLink(COMMIT)).isEqualTo(COMMIT);
        assertThat(decorator.asLink(COMMIT)).isEqualTo(COMMIT);
        assertThat(decorator.asText(COMMIT)).isEqualTo(COMMIT);
    }

    private CommitDecorator createDecorator(final String link) {
        return new CommitDecorator() {
            @Override
            public String asLink(final String id) {
                return link;
            }
        };
    }
}
