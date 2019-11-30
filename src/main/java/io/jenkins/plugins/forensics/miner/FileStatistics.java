package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.google.common.annotations.VisibleForTesting;

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
    private transient int today; // TODO: remove in 1.0.0

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file for which statistics will be generated
     */
    public FileStatistics(final String fileName) {
        this(fileName, nowInSecondsSinceEpoch());
    }

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file for which statistics will be generated
     * @param today
     *         today (given as number of seconds since the standard base time known as "the epoch", namely January 1,
     *         1970, 00:00:00 GMT.).
     */
    @VisibleForTesting
    public FileStatistics(final String fileName, final int today) {
        this.fileName = StringUtils.replace(fileName, WINDOWS_BACK_SLASH, UNIX_SLASH);
        this.today = today;
    }

    private static int nowInSecondsSinceEpoch() {
        return (int) (new Date().getTime() / 1000L);
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
        today = nowInSecondsSinceEpoch();

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
     * Returns the age of this file. It is given as the number of days starting from today. If the file has been created
     * today, then 0 is returned.
     *
     * @return the age in days (from now)
     * @deprecated will be removed in 1.0.0, use UI layer to compute that value
     */
    @Deprecated
    public int getAgeInDays() {
        if (numberOfCommits == 0) {
            return 0;
        }

        return computeDaysSince(creationTime);
    }

    /**
     * Returns the last modification time of this file. It is given as the number of days starting from today. If the
     * file has been modified today, then 0 is returned.
     *
     * @return the age in days (from now)
     * @deprecated will be removed in 1.0.0, use UI layer to compute that value
     */
    @Deprecated
    public int getLastModifiedInDays() {
        if (numberOfCommits == 0) {
            return 0;
        }

        return computeDaysSince(lastModificationTime);
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

    private int computeDaysSince(final int timeInSecondsSinceEpoch) {
        long days = Math.abs(ChronoUnit.DAYS.between(toLocalDate(today), toLocalDate(timeInSecondsSinceEpoch)));
        if (days > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) days;
    }

    private static LocalDate toLocalDate(final int timeInSecondsSinceEpoch) {
        return new Date(timeInSecondsSinceEpoch * 1000L)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
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
