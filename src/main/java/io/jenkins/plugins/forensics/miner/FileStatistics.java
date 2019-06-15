package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.google.common.annotations.VisibleForTesting;

/**
 * Aggregates commit statistics for a given file:
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

    private final String fileName;

    private int numberOfAuthors;
    private int numberOfCommits;
    private int creationTime;
    private int lastModificationTime;

    private transient Set<String> authors = new HashSet<>();
    private LocalDate today;

    /**
     * Creates a new instance of {@link FileStatistics}.
     *
     * @param fileName
     *         the name of the file that should be blamed
     */
    public FileStatistics(final String fileName) {
        this(fileName, LocalDate.now());
    }

    @VisibleForTesting
    FileStatistics(final String fileName, final int today) {
        this(fileName, toLocalDate(today));
    }

    private FileStatistics(final String fileName, final LocalDate today) {
        this.fileName = fileName;
        this.today = today;
    }

    public String getFileName() {
        return fileName;
    }

    private Object readResolve() {
        authors = new HashSet<>(); // restore an empty set since the authors set is used only during aggregation

        return this;
    }

    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

    public int getNumberOfCommits() {
        return numberOfCommits;
    }

    public long getAgeInDays() {
        if (numberOfCommits == 0) {
            return 0;
        }

        return computeDaysSince(creationTime);
    }

    public long getLastModifiedInDays() {
        if (numberOfCommits == 0) {
            return 0;
        }

        return computeDaysSince(lastModificationTime);
    }

    public void inspectCommit(final int commitTime, final String author) {
        if (numberOfCommits == 0) {
            lastModificationTime = commitTime;
        }
        creationTime = commitTime;
        numberOfCommits++;
        authors.add(author);
        numberOfAuthors = authors.size();
    }

    private long computeDaysSince(final int timeInSecondsSinceEpoch) {
        return Math.abs(ChronoUnit.DAYS.between(today, toLocalDate(timeInSecondsSinceEpoch)));
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
