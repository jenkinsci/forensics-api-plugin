package io.jenkins.plugins.forensics.miner;

import hudson.util.XStream2;

import io.jenkins.plugins.util.AbstractXmlStream;

/**
 * Reads {@link RepositoryStatistics} from an XML file.
 *
 * @author Ullrich Hafner
 */
public class RepositoryStatisticsXmlStream extends AbstractXmlStream<RepositoryStatistics> {
    /**
     * Creates a new {@link RepositoryStatisticsXmlStream}.
     */
    public RepositoryStatisticsXmlStream() {
        super(RepositoryStatistics.class);
    }

    @Override
    protected RepositoryStatistics createDefaultValue() {
        return new RepositoryStatistics();
    }

    @Override
    protected void configureXStream(final XStream2 xStream) {
        xStream.alias("diff", CommitDiffItem.class);
        xStream.alias("repo", RepositoryStatistics.class);
        xStream.alias("file", FileStatistics.class);
    }
}
