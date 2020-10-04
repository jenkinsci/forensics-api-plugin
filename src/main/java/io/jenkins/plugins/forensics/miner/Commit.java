package io.jenkins.plugins.forensics.miner;

import java.util.Objects;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.Generated;

/**
 * Represents an SCM commit. Commits are sorted by the modification time, starting with the newest commit and walking
 * back into the history. For each commit the number of added and deleted lines will be recorded. Since a commit
 * consists of a list of differences the number of added or deleted lines can be updated several times until the final
 * size has been reached.
 *
 * @author Ullrich Hafner
 */
public class Commit implements Comparable<Commit> {
    private final String id;
    private final String author;
    private final int time;

    private int totalAddedLines = 0;
    private int totalDeletedLines = 0;
    private boolean deleted = false;
    private String oldPath = StringUtils.EMPTY;

    /**
     * Creates a new {@link Commit}.
     *
     * @param id
     *         commit ID
     * @param author
     *         author of the commit
     * @param time
     *         the time of the commit (given as number of seconds since the standard base time known as "the epoch", *
     *         namely January 1, 1970, 00:00:00 GMT)
     */
    public Commit(final String id, final String author, final int time) {
        this.id = id;
        this.author = author;
        this.time = time;
    }

    /**
     * Creates a new copy of the specified {@link Commit}. Note that the number of added and deleted lines will be set
     * to zero, so this constructor is not a copy constructor in the common sense.
     *
     * @param copy
     *         the commit to copy the base properties from
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public Commit(final Commit copy) {
        this(copy.getId(), copy.getAuthor(), copy.getTime());
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public int getTime() {
        return time;
    }

    public int getTotalAddedLines() {
        return totalAddedLines;
    }

    public int getTotalDeletedLines() {
        return totalDeletedLines;
    }

    public String getOldPath() {
        return oldPath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public int compareTo(final Commit o) {
        return o.time - time;
    }

    /**
     * Adds a diff with the specified number of added lines to this commit.
     *
     * @param addedLines
     *         the additional number of added lines
     *
     * @return this
     */
    public Commit addLines(final int addedLines) {
        totalAddedLines += addedLines;

        return this;
    }

    /**
     * Adds a diff with the specified number of deleted lines to this commit.
     *
     * @param deletedLines
     *         the additional number of deleted lines
     *
     * @return this
     */
    public Commit deleteLines(final int deletedLines) {
        totalDeletedLines += deletedLines;

        return this;
    }

    /**
     * Sets the old path of a renamed file.
     *
     * @param oldPath the old path
     */
    public void setOldPath(final String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * Mark the commit as DELETE, e.g. a file has been deleted.
     */
    public void markAsDeleted() {
        deleted = true;
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
        Commit commit = (Commit) o;
        return time == commit.time && id.equals(commit.id) && author.equals(commit.author)
                && totalAddedLines == commit.totalAddedLines && totalDeletedLines == commit.totalDeletedLines;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, author, time, totalAddedLines, totalDeletedLines);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", Commit.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("author='" + author + "'")
                .add("time=" + time)
                .add("totalAddedLines=" + totalAddedLines)
                .add("totalDeletedLines=" + totalDeletedLines)
                .toString();
    }

}
