package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.Generated;

/**
 * Computes and stores aggregated statistics for a collection of commits.
 *
 * @author Ullrich Hafner
 */
public class CommitStatistics implements Serializable {
    private static final long serialVersionUID = 1L; // since 0.8.0

    private final int addedLines;
    private final int deletedLines;
    private final int authorCount;
    private final int commitCount;
    private final int filesCount;

    /**
     * Creates a new instance of {@link CommitStatistics}.
     *
     * @param commits
     *         the commits to aggregate the statistics for
     */
    public CommitStatistics(final Collection<? extends CommitDiffItem> commits) {
        addedLines = countAddedLines(commits);
        deletedLines = countDeletedLines(commits);
        authorCount = countAuthors(commits);
        commitCount = countCommits(commits);
        filesCount = (int) commits.stream()
                .map(CommitDiffItem::getNewPath)
                .distinct()
                .filter(name -> !CommitDiffItem.NO_FILE_NAME.equals(name))
                .count();
    }

    /**
     * Creates empty {@link CommitStatistics}.
     */
    public CommitStatistics() {
        this(Collections.emptyList());
    }

    /**
     * Creates a partially filled {@link CommitStatistics} instance.
     *
     * @param numberOfCommits
     *         number of commits
     * @param numberOfAuthors
     *         number of authors
     *
     * @deprecated just used for deserialization of existing old serialization files
     */
    @Deprecated
    public CommitStatistics(final int numberOfCommits, final int numberOfAuthors) {
        commitCount = numberOfCommits;
        authorCount = numberOfAuthors;
        addedLines = 0;
        deletedLines = 0;
        filesCount = 0;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public int getDeletedLines() {
        return deletedLines;
    }

    public int getLinesOfCode() {
        return addedLines - deletedLines;
    }

    public int getAbsoluteChurn() {
        return addedLines + deletedLines;
    }

    public int getAuthorCount() {
        return authorCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public int getFilesCount() {
        return filesCount;
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
        CommitStatistics that = (CommitStatistics) o;
        return addedLines == that.addedLines && deletedLines == that.deletedLines
                && authorCount == that.authorCount && commitCount == that.commitCount && filesCount == that.filesCount;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(addedLines, deletedLines, authorCount, commitCount, filesCount);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", CommitStatistics.class.getSimpleName() + "[", "]")
                .add("addedLines=" + addedLines)
                .add("deletedLines=" + deletedLines)
                .add("authorCount=" + authorCount)
                .add("commitCount=" + commitCount)
                .add("filesCount=" + filesCount)
                .toString();
    }

    private static int getDistinctCount(final Collection<? extends CommitDiffItem> commits,
            final Function<CommitDiffItem, String> property) {
        return (int) commits.stream().map(property).map(s -> String.toLowerCase(Locale.ENGLISH)).distinct().count();
    }

    private static int count(final Collection<? extends CommitDiffItem> commits, final ToIntFunction<CommitDiffItem> property) {
        return commits.stream().mapToInt(property).sum();
    }

    /**
     * Counts the number of RENAME commits. A rename commit is a commit where an existing file has been moved to a new
     * location.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of RENAME commits
     */
    public static int countMoves(final Collection<? extends CommitDiffItem> commits) {
        return (int) commits.stream().filter(CommitDiffItem::isMove).count();
    }

    /**
     * Counts the number of DELETE commits. A delete commit is a commit where an existing file has been deleted.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of DELETE commits
     */
    public static int countDeletes(final Collection<? extends CommitDiffItem> commits) {
        return (int) commits.stream().filter(CommitDiffItem::isDelete).count();
    }

    /**
     * Counts the number of CHANGE commits. A change commit is a commit where an existing file has been changed.
     *
     * @param commits
     *         the commits to analyze
     *
     * @return number of RENAME commits
     */
    public static int countChanges(final Collection<? extends CommitDiffItem> commits) {
        return (int) commits.stream().filter(commit -> !commit.hasOldPath()).count();
    }

    private static int countAddedLines(final Collection<? extends CommitDiffItem> commits) {
        return count(commits, CommitDiffItem::getTotalAddedLines);
    }

    private static int countDeletedLines(final Collection<? extends CommitDiffItem> commits) {
        return count(commits, CommitDiffItem::getTotalDeletedLines);
    }

    private static int countAuthors(final Collection<? extends CommitDiffItem> commits) {
        return getDistinctCount(commits, CommitDiffItem::getAuthor);
    }

    private static int countCommits(final Collection<? extends CommitDiffItem> commits) {
        return getDistinctCount(commits, CommitDiffItem::getId);
    }

    /**
     * Prints a summary of the specified commits to the specified logger.
     *
     * @param commits
     *         the commits to summarize
     * @param logger
     *         the logger
     */
    public static void logCommits(final List<CommitDiffItem> commits, final FilteredLog logger) {
        logger.logInfo("-> %d commits with differences analyzed", countCommits(commits));
        logIfPositive(countChanges(commits), "-> %d MODIFY commit diff items", logger);
        logIfPositive(countMoves(commits), "-> %d RENAME commit diff items", logger);
        logIfPositive(countDeletes(commits), "-> %d DELETE commit diff items", logger);
        logger.logInfo("-> %d lines added", countAddedLines(commits));
        logger.logInfo("-> %d lines deleted", countDeletedLines(commits));
    }

    private static void logIfPositive(final int total, final String message, final FilteredLog logger) {
        if (total > 0) {
            logger.logInfo(message, total);
        }
    }
}
