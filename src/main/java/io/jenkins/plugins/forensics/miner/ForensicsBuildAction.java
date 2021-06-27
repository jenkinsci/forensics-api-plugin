package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.NoSuchElementException;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Run;

import io.jenkins.plugins.forensics.reference.ReferenceFinder;
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

    private final int miningDurationSeconds;
    private final String urlName;

    private String scmKey; // since 0.9.0
    private String fileName; // since 0.9.0

    private final int numberOfFiles;
    private final int totalLinesOfCode; // since 1.1.0
    private final int totalChurn; // since 1.1.0
    private CommitStatistics commitStatistics;  // since 1.1.0

    private final int deltaNumberOfFiles; // since 1.2.0
    private final int deltaTotalLinesOfCode; // since 1.2.0
    private final int deltaTotalChurn; // since 1.2.0
    private CommitStatistics deltaCommitStatistics; // since 1.2.0

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
        this(owner, repositoryStatistics, new RepositoryStatistics(), miningDurationSeconds, scmKey, number);
    }

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param deltaStatistics
     *         the statistics compared to the reference build (if available)
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     * @param scmKey
     *         key of the repository
     * @param number
     *         unique number of the results (used as part of the serialization file name)
     */
    public ForensicsBuildAction(final Run<?, ?> owner,
            final RepositoryStatistics repositoryStatistics, final RepositoryStatistics deltaStatistics,
            final int miningDurationSeconds, final String scmKey, final int number) {
        this(owner, repositoryStatistics, deltaStatistics, true, miningDurationSeconds, scmKey, number);
    }

    @VisibleForTesting
    ForensicsBuildAction(final Run<?, ?> owner,
            final RepositoryStatistics repositoryStatistics, final RepositoryStatistics deltaStatistics,
            final boolean canSerialize, final int miningDurationSeconds, final String scmKey, final int number) {
        super(owner, repositoryStatistics, false);

        this.miningDurationSeconds = miningDurationSeconds;
        this.scmKey = scmKey;

        fileName = createFileName(number);
        urlName = createUrlName(number);

        numberOfFiles = repositoryStatistics.size();
        totalLinesOfCode = repositoryStatistics.getTotalLinesOfCode();
        totalChurn = repositoryStatistics.getTotalChurn();
        commitStatistics = repositoryStatistics.getLatestStatistics();

        deltaNumberOfFiles = deltaStatistics.size();
        deltaTotalLinesOfCode = deltaStatistics.getTotalLinesOfCode();
        deltaTotalChurn = deltaStatistics.getTotalChurn();
        deltaCommitStatistics = deltaStatistics.getLatestStatistics();

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
        if (commitStatistics == null) {
            commitStatistics = new CommitStatistics();
        }
        if (deltaCommitStatistics == null) {
            deltaCommitStatistics = new CommitStatistics();
        }

        return super.readResolve();
    }

    private String createFileName(final int number) {
        if (number == 0) {
            return DEFAULT_FILE_NAME;
        }
        return String.format("repository-statistics-%d.xml", number);
    }

    private String createUrlName(final int number) {
        if (number == 0) {
            return ForensicsJobAction.FORENSICS_ID;
        }
        return String.format("%s-%d", ForensicsJobAction.FORENSICS_ID, number);
    }

    @Override
    protected ForensicsJobAction createProjectAction() {
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
        return Messages.Forensics_Action();
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
        return urlName;
    }

    public int getMiningDurationSeconds() {
        return miningDurationSeconds;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public int getTotalChurn() {
        return totalChurn;
    }

    public CommitStatistics getCommitStatistics() {
        return commitStatistics;
    }

    public Run<?, ?> getReferenceBuild() {
        return new ReferenceFinder().findReference(getOwner(), new FilteredLog(""))
                .orElseThrow(() -> new NoSuchElementException("No reference build available"));
    }

    public int getDeltaNumberOfFiles() {
        return deltaNumberOfFiles;
    }

    public int getDeltaTotalLinesOfCode() {
        return deltaTotalLinesOfCode;
    }

    public int getDeltaTotalChurn() {
        return deltaTotalChurn;
    }

    public CommitStatistics getDeltaCommitStatistics() {
        return deltaCommitStatistics;
    }

    public String getScmKey() {
        return scmKey;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", urlName, scmKey);
    }
}
