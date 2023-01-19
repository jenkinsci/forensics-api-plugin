package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.SerializableTest;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link CommitStatistics}.
 *
 * @author Ullrich Hafner
 */
class CommitStatisticsTest extends SerializableTest<CommitStatistics> {
    private static final String AUTHOR = "author";
    private static final TreeStringBuilder BUILDER = new TreeStringBuilder();

    @Override
    protected CommitStatistics createSerializable() {
        List<CommitDiffItem> commits = new ArrayList<>();

        CommitDiffItem first = new CommitDiffItem("1", AUTHOR, 0);
        first.addLines(3).deleteLines(2);
        commits.add(first);
        CommitDiffItem second = new CommitDiffItem("2", "anotherAuthor", 2);
        second.addLines(3).deleteLines(4);
        commits.add(second);

        return new CommitStatistics(commits);
    }

    @Test
    @SuppressWarnings("checkstyle:JavaNCSS")
    void shouldCountCorrectly() {
        List<CommitDiffItem> commits = new ArrayList<>();

        assertThat(CommitStatistics.countChanges(commits)).isZero();
        assertThat(CommitStatistics.countDeletes(commits)).isZero();
        assertThat(CommitStatistics.countMoves(commits)).isZero();

        CommitStatistics empty = new CommitStatistics(commits);
        assertThat(empty).hasAddedLines(0)
                .hasDeletedLines(0)
                .hasLinesOfCode(0)
                .hasAbsoluteChurn(0)
                .hasAuthorCount(0)
                .hasCommitCount(0);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 0 commits with differences analyzed",
                "-> 0 lines added",
                "-> 0 lines deleted");

        CommitDiffItem first = new CommitDiffItem("1", AUTHOR, 0);
        first.addLines(3).deleteLines(2);
        commits.add(first);

        assertThat(CommitStatistics.countChanges(commits)).isOne();
        assertThat(CommitStatistics.countDeletes(commits)).isZero();
        assertThat(CommitStatistics.countMoves(commits)).isZero();
        CommitStatistics firstCommit = new CommitStatistics(commits);
        assertThat(firstCommit).hasAddedLines(3)
                .hasDeletedLines(2)
                .hasLinesOfCode(1)
                .hasAbsoluteChurn(5)
                .hasAuthorCount(1)
                .hasCommitCount(1);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 1 commits with differences analyzed",
                "-> 1 MODIFY commit diff items",
                "-> 3 lines added",
                "-> 2 lines deleted");

        CommitDiffItem second = new CommitDiffItem("2", "anotherAuthor", 2);
        second.addLines(3).deleteLines(4);
        commits.add(second);

        assertThat(CommitStatistics.countChanges(commits)).isEqualTo(2);
        assertThat(CommitStatistics.countDeletes(commits)).isZero();
        assertThat(CommitStatistics.countMoves(commits)).isZero();
        CommitStatistics secondCommit = new CommitStatistics(commits);
        assertThat(secondCommit).hasAddedLines(6)
                .hasDeletedLines(6)
                .hasLinesOfCode(0)
                .hasAbsoluteChurn(12)
                .hasAuthorCount(2)
                .hasCommitCount(2);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 2 commits with differences analyzed",
                "-> 2 MODIFY commit diff items",
                "-> 6 lines added",
                "-> 6 lines deleted");

        CommitDiffItem third = new CommitDiffItem("2", AUTHOR, 2);
        third.setNewPath(asTreeString(CommitDiffItem.NO_FILE_NAME));
        third.setOldPath(asTreeString("old"));
        commits.add(third);

        assertThat(CommitStatistics.countChanges(commits)).isEqualTo(2);
        assertThat(CommitStatistics.countDeletes(commits)).isEqualTo(1);
        assertThat(CommitStatistics.countMoves(commits)).isZero();
        CommitStatistics thirdCommit = new CommitStatistics(commits);
        assertThat(thirdCommit).hasAddedLines(6)
                .hasDeletedLines(6)
                .hasLinesOfCode(0)
                .hasAbsoluteChurn(12)
                .hasAuthorCount(2)
                .hasCommitCount(2);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 2 commits with differences analyzed",
                "-> 2 MODIFY commit diff items",
                "-> 1 DELETE commit diff items",
                "-> 6 lines added",
                "-> 6 lines deleted");

        CommitDiffItem forth = new CommitDiffItem("3", AUTHOR, 3);
        forth.setNewPath(asTreeString("new"));
        forth.setOldPath(asTreeString("old"));
        commits.add(forth);

        assertThat(CommitStatistics.countChanges(commits)).isEqualTo(2);
        assertThat(CommitStatistics.countDeletes(commits)).isEqualTo(1);
        assertThat(CommitStatistics.countMoves(commits)).isEqualTo(1);
        CommitStatistics forthCommit = new CommitStatistics(commits);
        assertThat(forthCommit).hasAddedLines(6)
                .hasDeletedLines(6)
                .hasLinesOfCode(0)
                .hasAbsoluteChurn(12)
                .hasAuthorCount(2)
                .hasCommitCount(3);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 3 commits with differences analyzed",
                "-> 2 MODIFY commit diff items",
                "-> 1 RENAME commit diff items",
                "-> 1 DELETE commit diff items",
                "-> 6 lines added",
                "-> 6 lines deleted");
    }

    @Test
    void shouldIgnoreCapitalizationOfEmailNames() {
        List<CommitDiffItem> commits = new ArrayList<>();

        CommitDiffItem first = new CommitDiffItem("1", "theauthor@mailto.me", 0);
        commits.add(first);

        CommitStatistics firstCommit = new CommitStatistics(commits);
        assertThat(firstCommit).hasAuthorCount(1);

        CommitDiffItem second = new CommitDiffItem("2", "Theauthor@mailto.me", 0);
        commits.add(second);

        CommitStatistics secondCommit = new CommitStatistics(commits);
        assertThat(secondCommit).hasAuthorCount(1);

    }

    private TreeString asTreeString(final String old) {
        return BUILDER.intern(old);
    }

    private FilteredLog logCommits(final List<CommitDiffItem> commits) {
        FilteredLog log = new FilteredLog("Error");
        CommitStatistics.logCommits(commits, log);
        return log;
    }
}
