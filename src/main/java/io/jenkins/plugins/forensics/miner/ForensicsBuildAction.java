package io.jenkins.plugins.forensics.miner;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Action;
import hudson.model.Run;

import io.jenkins.plugins.util.BuildAction;

/**
 * Controls the live cycle of the forensics results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the forensics details: these are rendered using a new view instance.
 *
 * @author Ullrich Hafner
 */
public class ForensicsBuildAction extends BuildAction<RepositoryStatistics> implements StaplerProxy {
    private static final long serialVersionUID = -263122257268060032L;
    private static final String DEFAULT_FILE_NAME = "repository-statistics.xml";

    private final int numberOfFiles;
    private final int miningDurationSeconds;

    private String scmKey; // since 0.9.0
    private String fileName; // since 0.9.0

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     * @deprecated use {@link #ForensicsBuildAction(Run, RepositoryStatistics, int, String, int)}
     */
    @Deprecated
    public ForensicsBuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final int miningDurationSeconds) {
        this(owner, repositoryStatistics, true, miningDurationSeconds, StringUtils.EMPTY, 0);
    }

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     * @param scmKey
     *         key of the repository
     * @param number
     *         unique number of the results (used as part of the serialization file name)
     */
    public ForensicsBuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final int miningDurationSeconds, final String scmKey, final int number) {
        this(owner, repositoryStatistics, true, miningDurationSeconds, scmKey, number);
    }

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     * @param scmKey
     *         key of the repository
     * @param number
     *         unique number of the results (used as part of the serialization file name)
     */
    @VisibleForTesting
    ForensicsBuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final boolean canSerialize, final int miningDurationSeconds, final String scmKey, final int number) {
        super(owner, repositoryStatistics, false);

        numberOfFiles = repositoryStatistics.size();
        this.miningDurationSeconds = miningDurationSeconds;
        this.scmKey = scmKey;
        fileName = getFileName(number);

        if (canSerialize) {
            createXmlStream().write(owner.getRootDir().toPath().resolve(fileName), repositoryStatistics);
        }
    }

    @Override
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (scmKey == null) {
            scmKey = StringUtils.EMPTY;
        }
        if (fileName == null) {
            fileName = DEFAULT_FILE_NAME;
        }
        return super.readResolve();
    }

    private String getFileName(final int number) {
        if (number == 0) {
            return DEFAULT_FILE_NAME;
        }
        return String.format("repository-statistics-%d.xml", number);
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Arrays.asList(new ForensicsJobAction(getOwner().getParent(), scmKey),
                new ForensicsCodeMetricAction(getOwner().getParent(), scmKey));
    }

    @Override
    protected ForensicsJobAction createProjectAction() {
        // This method actually is obsolete and will not be called anymore
        return new ForensicsJobAction(getOwner().getParent(), scmKey);
    }

    @Override
    protected RepositoryStatisticsXmlStream createXmlStream() {
        return new RepositoryStatisticsXmlStream();
    }

    @Override
    protected String getBuildResultBaseName() {
        return fileName;
    }

    @Override
    public String getIconFileName() {
        return ForensicsJobAction.SMALL_ICON;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title(scmKey);
    }

    /**
     * Returns the detail view for the forensics data for all Stapler requests.
     *
     * @return the detail view for the forensics data
     */
    @Override
    public Object getTarget() {
        return new ForensicsViewModel(getOwner(), getResult(), scmKey);
    }

    @Override
    public String getUrlName() {
        return ForensicsJobAction.FORENSICS_ID;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getMiningDurationSeconds() {
        return miningDurationSeconds;
    }

    public String getScmKey() {
        return scmKey;
    }
}
