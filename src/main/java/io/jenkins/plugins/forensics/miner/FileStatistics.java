package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import io.jenkins.plugins.forensics.blame.FileBlame;

/**
 * Aggregates commit statistics for a given file. The following statistics are summed up:
 * <ul>
 *     <li>total number of commits</li>
 *     <li>total number of different authors</li>
 *     <li>creation time</li>
 *     <li>last modification time</li>
 *     <li>added lines</li>
 *     <li>deleted lines</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public final class FileStatistics implements Serializable {
    @Serial
    private static final long serialVersionUID = 8L; // release 0.8.x

    private TreeString fileName;

    private int creationTime;
    private int lastModificationTime;

    private transient int numberOfAuthors; // unused starting from 0.8.x
    private transient int numberOfCommits; // unused starting from 0.8.x

    private CommitStatistics statistics = new CommitStatistics(); // since 0.8.0
    @SuppressWarnings("PMD.LooseCoupling")
    private ArrayList<CommitDiffItem> commits = new ArrayList<>(); // since 0.8.0

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file for which statistics will be generated
     * @see FileStatisticsBuilder
     */
    private FileStatistics(final TreeString fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName.toString();
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @Serial
    @SuppressWarnings("deprecation")
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    private Object readResolve() {
        if (commits == null) {
            commits = new ArrayList<>(); // restore an empty list for release < 0.8.x
            statistics = new CommitStatistics(numberOfCommits, numberOfAuthors);
        }

        return this;
    }

    /**
     * Returns all commits this file was part of.
     *
     * @return all commits for this file
     */
    public List<CommitDiffItem> getCommits() {
        return commits;
    }

    /**
     * Returns the number of authors for this file.
     *
     * @return the number authors for this file.
     */
    public int getNumberOfAuthors() {
        return statistics.getAuthorCount();
    }

    /**
     * Returns the number of times this file was committed.
     *
     * @return the number of commits for this file
     */
    public int getNumberOfCommits() {
        return statistics.getCommitCount();
    }

    /**
     * Returns the creation time of this file.
     *
     * @return the time of the creation (given as number of seconds since the standard base time known as "the epoch",
     *         namely January 1, 1970, 00:00:00 GMT).
     */
    public int getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the time of the last modification of this file (i.e. last commit to the file).
     *
     * @return the time of the last modification (given as number of seconds since the standard base time known as "the
     *         epoch", namely January 1, 1970, 00:00:00 GMT).
     */
    public int getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * Returns the total lines of code for this file.
     *
     * @return the total lines of code.
     */
    public int getLinesOfCode() {
        return statistics.getLinesOfCode();
    }

    /**
     * Returns the absolute churn for this file, i.e. the sum of all added and deleted lines.
     *
     * @return absolute churn
     */
    public int getAbsoluteChurn() {
        return statistics.getAbsoluteChurn();
    }

    /**
     * Inspects and stores the specified commit for this file. Updates all properties after the commit has been added,
     * including an optional file name rename.
     *
     * @param additionalCommit
     *         the additional commit to inspect
     */
    public void inspectCommit(final CommitDiffItem additionalCommit) {
        commits.add(additionalCommit);

        updateProperties();
    }

    /**
     * Inspects and stores the specified commits for this file. Updates all properties after the commit has been added,
     * including an optional file name rename.
     *
     * @param additionalCommits
     *         the additional commits to inspect
     */
    public void inspectCommits(final Collection<CommitDiffItem> additionalCommits) {
        commits.addAll(additionalCommits);

        updateProperties();
    }

    private void updateProperties() {
        int lastCommit = commits.size() - 1;
        lastModificationTime = commits.get(lastCommit).getTime();
        creationTime = commits.get(0).getTime();
        statistics = new CommitStatistics(commits);
        fileName = TreeString.valueOf(commits.get(lastCommit).getNewPath());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (FileStatistics) o;
        return creationTime == that.creationTime && lastModificationTime == that.lastModificationTime
                && Objects.equals(fileName, that.fileName) && Objects.equals(statistics, that.statistics)
                && Objects.equals(commits, that.commits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, creationTime, lastModificationTime, statistics, commits);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FileStatistics.class.getSimpleName() + "[", "]")
                .add("fileName=" + fileName)
                .add("creationTime=" + creationTime)
                .add("lastModificationTime=" + lastModificationTime)
                .add("statistics=" + statistics)
                .toString();
    }

    /**
     * Creates {@link FileBlame} instances that optimize the memory footprint for file names by using a {@link
     * TreeStringBuilder}.
     */
    public static class FileStatisticsBuilder {
        private final TreeStringBuilder builder = new TreeStringBuilder();
        private final PathUtil pathUtil = new PathUtil();

        /**
         * Creates a new {@link FileStatistics} instance for the specified file name. The file name will be normalized
         * and compressed using a {@link TreeStringBuilder}.
         *
         * @param fileName
         *         the file name
         *
         * @return the created {@link FileStatistics} instance
         */
        public FileStatistics build(final String fileName) {
            return new FileStatistics(builder.intern(pathUtil.getAbsolutePath(fileName)));
        }
    }
}
