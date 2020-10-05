package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.Generated;

/**
 * Represents an SCM commit. Commits are sorted by the modification time, starting with the newest commit and walking
 * back into the history. For each commit the number of added and deleted lines will be recorded. Since a commit
 * consists of a list of differences the number of added or deleted lines can be updated several times until the final
 * size has been reached.
 *
 * @author Ullrich Hafner
 */
public class Commit implements Serializable {
    private static final long serialVersionUID = 1L; // since 0.8.0

    /** Indicates that a file name has not been set or a file has been deleted. */
    static final String NO_FILE_NAME = "/dev/null";

    /**
     * Counts the number of RENAME commits. A rename commit is a commit where an existing file has been moved to a new
     * location.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of RENAME commits
     */
    public static int countMoves(final Collection<? extends Commit> commits) {
        return (int) commits.stream().filter(Commit::isMove).count();
    }

    /**
     * Counts the number of DELETE commits. A delete commit is a commit where an existing file has been deleted.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of DELETE commits
     */
    public static int countDeletes(final Collection<? extends Commit> commits) {
        return (int) commits.stream().filter(Commit::isDelete).count();
    }

    /**
     * Counts the number of CHANGE commits. A change commit is a commit where an existing file has been changed.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of RENAME commits
     */
    public static int countChanges(final Collection<? extends Commit> commits) {
        return (int) commits.stream().filter(commit -> !commit.hasOldPath()).count();
    }

    /**
     * Counts the total number of added lines by all commits.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return total number of added lines
     */
    public static int countAddedLines(final Collection<? extends Commit> commits) {
        return count(commits, Commit::getTotalAddedLines);
    }

    /**
     * Counts the total number of deleted lines by all commits.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return total number of deleted lines
     */
    public static int countDeletedLines(final Collection<? extends Commit> commits) {
        return count(commits, Commit::getTotalDeletedLines);
    }

    /**
     * Counts the total number of distinct authors in all commits.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return total number of distinct authors
     */
    public static int countAuthors(final Collection<? extends Commit> commits) {
        return getDistinctCount(commits, Commit::getAuthor);
    }

    /**
     * Counts the total number of distinct commit IDs in all commits.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return total number of distinct commit IDs
     */
    public static int countCommits(final Collection<? extends Commit> commits) {
        return getDistinctCount(commits, Commit::getId);
    }

    private static int getDistinctCount(final Collection<? extends Commit> commits,
            final Function<Commit, String> property) {
        return (int) commits.stream().map(property).distinct().count();
    }

    private static int count(final Collection<? extends Commit> commits, final ToIntFunction<Commit> property) {
        return commits.stream().mapToInt(property).sum();
    }

    /**
     * Prints a summary of the specified commits to the specified logger.
     *
     * @param commits
     *         the commits to summarize
     * @param logger
     *         the logger     */
    public static void logCommits(final List<Commit> commits, final FilteredLog logger) {
        logger.logInfo("-> %d commits analyzed", Commit.countCommits(commits));
        logIfPositive(Commit.countChanges(commits), "-> %d MODIFY commits", logger);
        logIfPositive(Commit.countMoves(commits), "-> %d RENAME commits", logger);
        logIfPositive(Commit.countDeletes(commits), "-> %d DELETE commits", logger);
        logger.logInfo("-> %d lines added", Commit.countAddedLines(commits));
        logger.logInfo("-> %d lines added", Commit.countDeletedLines(commits));
    }

    private static void logIfPositive(final int total, final String message, final FilteredLog logger) {
        if (total > 0) {
            logger.logInfo(message, total);
        }
    }

    private final String id;

    private final String author;
    private final int time;
    private int totalAddedLines = 0;
    private int totalDeletedLines = 0;

    private String oldPath = NO_FILE_NAME;
    private String newPath = NO_FILE_NAME;

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

    public boolean isDelete() {
        return hasOldPath() && !hasNewPath();
    }

    public boolean isMove() {
        return hasOldPath() && hasNewPath();
    }

    /**
     * Returns whether the {@code oldPath} for this commit has been set. This indicates that the commit contains a moved
     * or removed file.
     *
     * @return {@code true} if the {@code oldPath} has been set, {@code false} otherwise
     */
    boolean hasOldPath() {
        return !NO_FILE_NAME.equals(getOldPath());
    }

    public String getNewPath() {
        return newPath;
    }

    /**
     * Returns whether the {@code newPath} for this commit has been set. If this path is not set, then this commit
     * contains a removed file.
     *
     * @return {@code true} if the {@code newPath} has been set, {@code false} otherwise
     */
    private boolean hasNewPath() {
        return !NO_FILE_NAME.equals(getNewPath());
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
     * Sets the old path of a moved or deleted file. If a file has been moved then the {@code newPath} contains the new
     * file name while the {@code oldPath} contains the old file name. It a file has been deleted, then the {@code
     * newPath} should be set to {@link #NO_FILE_NAME} while the {@code oldPath} contains the name of the deleted file.
     *
     * @param oldPath
     *         the path of the modified path
     *
     * @return this
     */
    public Commit setOldPath(final String oldPath) {
        this.oldPath = oldPath;

        return this;
    }

    /**
     * Sets the path of the modified file. If a file has been moved then the {@code newPath} contains the new file name
     * while the {@code oldPath} contains the old file name. It a file has been deleted, then the {@code newPath} should
     * be set to {@link #NO_FILE_NAME} while the {@code oldPath} contains the name of the deleted file.
     *
     * @param newPath
     *         the path of the modified path
     *
     * @return this
     */
    public Commit setNewPath(final String newPath) {
        this.newPath = newPath;

        return this;
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
        return time == commit.time
                && totalAddedLines == commit.totalAddedLines && totalDeletedLines == commit.totalDeletedLines
                && id.equals(commit.id) && author.equals(commit.author)
                && oldPath.equals(commit.oldPath) && newPath.equals(commit.newPath);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, author, time, totalAddedLines, totalDeletedLines, oldPath, newPath);
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
                .add("oldPath='" + oldPath + "'")
                .add("newPath='" + newPath + "'")
                .toString();
    }
}
