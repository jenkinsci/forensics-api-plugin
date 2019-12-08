package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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
    private static final long serialVersionUID = -5776167206905031327L;

    private static final String UNIX_SLASH = "/";
    private static final String WINDOWS_BACK_SLASH = "\\";

    private final String fileName;

    private int numberOfAuthors;
    private int numberOfCommits;
    private int creationTime;
    private int lastModificationTime;

    private transient Set<String> authors = new HashSet<>(); // see readResolve

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file for which statistics will be generated
     */
    public FileStatistics(final String fileName) {
        this.fileName = StringUtils.replace(fileName, WINDOWS_BACK_SLASH, UNIX_SLASH);
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        authors = new HashSet<>(); // restore an empty set since the authors set is used only during aggregation

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
                && lastModificationTime == that.lastModificationTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfAuthors, numberOfCommits, creationTime, lastModificationTime);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
