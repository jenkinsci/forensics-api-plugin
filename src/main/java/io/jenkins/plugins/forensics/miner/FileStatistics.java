package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class FileStatistics implements Serializable {
    private static final long serialVersionUID = 8L; // release 0.8.x

    private final TreeString fileName;

    private int numberOfAuthors;
    private int numberOfCommits;
    private int creationTime;
    private int lastModificationTime;

    private int linesOfCode;
    private int churn;

    private Map<String, Integer> addedLinesOfCommit = new LinkedHashMap<>();
    private Map<String, Integer> deletedLinesOfCommit = new LinkedHashMap<>();
    private Map<String, String> authorOfCommit = new LinkedHashMap<>();

    private List<String> commits = new LinkedList<>();

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

    public int getChurn() {
        return churn;
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (authorOfCommit == null) {
            authorOfCommit = new LinkedHashMap<>(); // restore an empty map for release < 0.8.x
        }
        else {
            authorOfCommit = authorOfCommit.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().intern(),
                            entry -> entry.getValue().intern())); // try to minimize memory
        }
        addedLinesOfCommit = readResolve(addedLinesOfCommit);
        deletedLinesOfCommit = readResolve(deletedLinesOfCommit);

        if (commits == null) {
            commits = new LinkedList<>();
        }
        else {
            commits = commits.stream().map(String::intern).collect(Collectors.toCollection(LinkedList::new));
        }

        return this;
    }

    private Map<String, Integer> readResolve(final Map<String, Integer> mapToResolve) {
        if (mapToResolve == null) {
            return new LinkedHashMap<>();
        }
        else {
            return mapToResolve.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().intern(),
                            Entry::getValue));
        }
    }

    /**
     * Returns all authors of this file.
     * @return all authors of this file
     */
    public Map<String, String> getAuthorOfCommit() {
        return authorOfCommit;
    }

    /**
     * Returns the author for this file for a specific commit.
     * @param commitId the id of the commit
     * @return the author of this commit
     */
    public String getAuthor(final String commitId) {
        return authorOfCommit.get(commitId);
    }

    /**
     * Returns the number of authors for this file.
     * @return the number authors for this file.
     */
    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

    /**
     * Returns the number of times this file was committed.
     * @return the number of commits for this file
     */
    public int getNumberOfCommits() {
        return numberOfCommits;
    }

    /**
     * Returns all commit ids for this file.
     * @return all commit ids for this file
     */
    public List<String> getCommits() {
        return commits;
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
        return linesOfCode;
    }

    /**
     * Returns all added lines to each commit.
     *
     * @return all added lines to each commit.
     */
    public Map<String, Integer> getAddedLinesOfCommit() {
        return addedLinesOfCommit;
    }

    /**
     * Returns the number of added lines to a specific commit id.
     * @param commitId the commit id
     * @return the number of added lines
     */
    public int getAddedLines(final String commitId) {
        return addedLinesOfCommit.get(commitId);
    }

    /**
     * Returns all deleted lines to each commit.
     *
     * @return all deleted lines to each commit.
     */
    public Map<String, Integer> getDeletedLinesOfCommit() {
        return deletedLinesOfCommit;
    }

    /**
     * Returns the number of deleted lines to a specific commit id.
     * @param commitId the commit id
     * @return the number of deleted lines
     */
    public int getDeletedLines(final String commitId) {
        return deletedLinesOfCommit.get(commitId);
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
     */
    public void inspectCommit(final int commitTime, final String author) {
        if (numberOfCommits == 0) {
            lastModificationTime = commitTime;
        }
        creationTime = commitTime;
        numberOfCommits++;
    }

    /**
     * Sets the value of lines of code to 0. This is used if a file is moved.
     */
    public void resetLinesOfCode() {
        this.linesOfCode = 0;
    }

    /**
     * Inspects the next commit for this file. The commits should be inspected in a sorted way, i.e. starting with the
     * newest commit until the first commit has been reached.
     *
     * @param commitTime
     *         the time of the commit (given as number of seconds since the standard base time known as "the epoch", *
     *         namely January 1, 1970, 00:00:00 GMT.).
     * @param author
     *         author (or comitter) name
     * @param totalLinesOfCode
     *         total lines of code of this file
     * @param commitId
     *         the id of the commit this file was checked in.
     * @param addedLines
     *         the number of added lines
     * @param removedLines
     *         the number of deleted lines
     */
    public void inspectCommit(final int commitTime, final String author, final int totalLinesOfCode,
            final String commitId, final int addedLines, final int removedLines) {
        inspectCommit(commitTime, author);
        if (numberOfCommits == 0) {
            linesOfCode = totalLinesOfCode;
        }
        else {
            linesOfCode += addedLines;
            linesOfCode -= removedLines;
        }
        commits.add(commitId);
        churn += addedLines + removedLines;
        addedLinesOfCommit.put(commitId, addedLines);
        deletedLinesOfCommit.put(commitId, removedLines);
        authorOfCommit.put(commitId, author);
        numberOfAuthors = (int) authorOfCommit.values().stream().distinct().count();

    }

    /**
     * Resets the churn of this file.
     */
    public void resetChurn() {
        churn = 0;
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
                && linesOfCode == that.linesOfCode
                && churn == that.churn
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(addedLinesOfCommit, that.addedLinesOfCommit)
                && Objects.equals(deletedLinesOfCommit, that.deletedLinesOfCommit)
                && Objects.equals(authorOfCommit, that.authorOfCommit);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(fileName, numberOfAuthors, numberOfCommits, creationTime, lastModificationTime, linesOfCode,
                churn, addedLinesOfCommit, deletedLinesOfCommit, authorOfCommit);
    }

    @Override
    @Generated
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
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
