package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

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

    private Map<String, Integer> numberOfAddedLines = new LinkedHashMap<>();
    private Map<String, Integer> numberOfDeletedLines = new LinkedHashMap<>();

    private Set<String> authors = new HashSet<>(); // see readResolve

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
    protected Object readResolve() {
        if (authors == null) {
            authors = new HashSet<>(); // restore an empty set for release < 0.8.x
        }
        else {
            authors = authors.stream().map(String::intern).collect(Collectors.toSet()); // try to minimize memory
        }
        return this;
    }

    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

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
        return linesOfCode;
    }

    /**
     * Returns all added lines to each commit.
     *
     * @return all added lines to each commit.
     */
    public Map<String, Integer> getNumberOfAddedLines() {
        return numberOfAddedLines;
    }

    /**
     * Returns all deleted lines to each commit.
     *
     * @return all deleted lines to each commit.
     */
    public Map<String, Integer> getNumberOfDeletedLines() {
        return numberOfDeletedLines;
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
        authors.add(author);
        numberOfAuthors = authors.size();
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
            linesOfCode += totalLinesOfCode;
        }
        churn += addedLines + removedLines;
        numberOfAddedLines.put(commitId, addedLines);
        numberOfDeletedLines.put(commitId, removedLines);
    }

    /**
     * Resets the churn of this file.
     */
    public void resetChurn() {
        churn = 0;
    }

    @Override
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
                && fileName.equals(that.fileName)
                && Objects.equals(authors, that.authors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, numberOfAuthors, numberOfCommits, creationTime, lastModificationTime, authors);
    }

    @Override
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
