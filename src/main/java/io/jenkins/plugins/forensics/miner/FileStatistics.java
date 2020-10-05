package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import edu.hm.hafner.util.Generated;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
public class FileStatistics implements Serializable {
    private static final long serialVersionUID = 8L; // release 0.8.x

    private TreeString fileName;

    private int numberOfAuthors;
    private int numberOfCommits;
    private int creationTime;
    private int lastModificationTime;
    private int addedLines = 0;
    private int deletedLines = 0;

    private List<Commit> commits = new ArrayList<>();

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file for which statistics will be generated
     */
    private FileStatistics(final TreeString fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName.toString();
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (commits == null) {
            commits = new ArrayList<>(); // restore an empty list for release < 0.8.x
        }

        return this;
    }

    /**
     * Returns all commits this file was part of.
     *
     * @return all commits for this file
     */
    public List<Commit> getCommits() {
        return commits;
    }

    /**
     * Returns the number of authors for this file.
     *
     * @return the number authors for this file.
     */
    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

    /**
     * Returns the number of times this file was committed.
     *
     * @return the number of commits for this file
     */
    public int getNumberOfCommits() {
        return numberOfCommits;
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
        return addedLines - deletedLines;
    }

    /**
     * Returns the absolute churn for this file, i.e. the sum of all added and deleted lines.
     *
     * @return absolute churn
     */
    public int getAbsoluteChurn() {
        return addedLines + deletedLines;
    }

    /**
     * Inspects the next commit for this file. The commits should be inspected in a sorted way, i.e. starting with the
     * newest commit until the first commit has been reached.
     *
     * @param commitTime
     *         the time of the commit (given as number of seconds since the standard base time known as "the epoch",
     *         namely January 1, 1970, 00:00:00 GMT.).
     * @param author
     *         author (or committer) name
     *
     * @deprecated remove before 1.0.0
     */
    @Deprecated
    public void inspectCommit(final int commitTime, final String author) {
        // not useful anymore
    }

    /**
     * Inspects and stores the specified commit for this file. Updates all properties after the commit has been added,
     * including an optional file name rename.
     *
     * @param additionalCommit
     *         the additional commit to inspect
     */
    public void inspectCommit(final Commit additionalCommit) {
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
    public void inspectCommits(final Collection<Commit> additionalCommits) {
        commits.addAll(additionalCommits);

        updateProperties();
    }

    private void updateProperties() {
        int lastCommit = commits.size() - 1;
        lastModificationTime = commits.get(lastCommit).getTime();
        creationTime = commits.get(0).getTime();
        numberOfCommits = commits.size();
        addedLines = Commit.countAddedLines(commits);
        deletedLines = Commit.countDeletedLines(commits);
        numberOfAuthors = Commit.countAuthors(commits);
        fileName = TreeString.valueOf(commits.get(lastCommit).getNewPath());
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileStatistics that = (FileStatistics) o;
        return numberOfAuthors == that.numberOfAuthors
                && numberOfCommits == that.numberOfCommits
                && creationTime == that.creationTime
                && lastModificationTime == that.lastModificationTime
                && addedLines == that.addedLines
                && deletedLines == that.deletedLines
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(commits, that.commits);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(fileName, numberOfAuthors, numberOfCommits, creationTime, lastModificationTime, addedLines,
                deletedLines, commits);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", FileStatistics.class.getSimpleName() + "[", "]")
                .add("fileName=" + fileName)
                .add("numberOfAuthors=" + numberOfAuthors)
                .add("numberOfCommits=" + numberOfCommits)
                .add("creationTime=" + creationTime)
                .add("lastModificationTime=" + lastModificationTime)
                .add("addedLines=" + addedLines)
                .add("deletedLines=" + deletedLines)
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
