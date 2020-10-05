package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.SerializableTest;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link Commit}.
 *
 * @author Ullrich Hafner
 */
class CommitTest extends SerializableTest<Commit> {
    private static final String ID = "ID";
    private static final String AUTHOR = "author";
    private static final int COMMITTED_AT = 1;

    @Test
    void shouldCreateEmptyCommit() {
        Commit commit = new Commit(ID, AUTHOR, COMMITTED_AT);

        verifyEmptyCommit(commit);

        Commit another = new Commit(commit);

        verifyEmptyCommit(another);
    }

    private void verifyEmptyCommit(final Commit commit) {
        assertThat(commit).hasId(ID)
                .hasAuthor(AUTHOR)
                .hasTime(COMMITTED_AT)
                .hasTotalAddedLines(0)
                .hasTotalDeletedLines(0)
                .hasNewPath(Commit.NO_FILE_NAME)
                .hasOldPath(Commit.NO_FILE_NAME)
                .isNotDelete()
                .isNotMove();
    }

    @Test
    void shouldChangeLines() {
        Commit commit = new Commit(ID, AUTHOR, COMMITTED_AT);

        commit.addLines(5);
        assertThat(commit).hasTotalAddedLines(5).hasTotalDeletedLines(0);

        commit.addLines(3);
        assertThat(commit).hasTotalAddedLines(8).hasTotalDeletedLines(0);

        commit.deleteLines(3);
        assertThat(commit).hasTotalAddedLines(8).hasTotalDeletedLines(3);

        commit.deleteLines(5);
        assertThat(commit).hasTotalAddedLines(8).hasTotalDeletedLines(8);
    }

    @Test
    void shouldAddPaths() {
        Commit commit = new Commit(ID, AUTHOR, COMMITTED_AT);

        commit.setNewPath("new");

        assertThat(commit)
                .hasNewPath("new")
                .hasOldPath(Commit.NO_FILE_NAME)
                .isNotMove()
                .isNotDelete();

        commit.setOldPath("old");
        assertThat(commit)
                .hasNewPath("new")
                .hasOldPath("old")
                .isMove()
                .isNotDelete();

        commit.setNewPath(Commit.NO_FILE_NAME);
        assertThat(commit)
                .hasNewPath(Commit.NO_FILE_NAME)
                .hasOldPath("old")
                .isNotMove()
                .isDelete();
    }

    @Test
    @SuppressWarnings("checkstyle:JavaNCSS")
    void shouldCountCorrectly() {
        List<Commit> commits = new ArrayList<>();

        assertThat(Commit.countAddedLines(commits)).isZero();
        assertThat(Commit.countDeletedLines(commits)).isZero();
        assertThat(Commit.countAuthors(commits)).isZero();
        assertThat(Commit.countChanges(commits)).isZero();
        assertThat(Commit.countDeletes(commits)).isZero();
        assertThat(Commit.countMoves(commits)).isZero();
        assertThat(Commit.countCommits(commits)).isZero();

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 0 commits analyzed",
                "-> 0 lines added",
                "-> 0 lines added");

        Commit first = new Commit("1", AUTHOR, 0);
        first.addLines(1).deleteLines(2);
        commits.add(first);

        assertThat(Commit.countAddedLines(commits)).isEqualTo(1);
        assertThat(Commit.countDeletedLines(commits)).isEqualTo(2);
        assertThat(Commit.countAuthors(commits)).isOne();
        assertThat(Commit.countChanges(commits)).isOne();
        assertThat(Commit.countDeletes(commits)).isZero();
        assertThat(Commit.countMoves(commits)).isZero();
        assertThat(Commit.countCommits(commits)).isOne();

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 1 commits analyzed",
                "-> 1 MODIFY commits",
                "-> 1 lines added",
                "-> 2 lines added");

        Commit second = new Commit("2", "anotherAuthor", 2);
        second.addLines(3).deleteLines(4);
        commits.add(second);

        assertThat(Commit.countAddedLines(commits)).isEqualTo(1 + 3);
        assertThat(Commit.countDeletedLines(commits)).isEqualTo(2 + 4);
        assertThat(Commit.countAuthors(commits)).isEqualTo(2);
        assertThat(Commit.countChanges(commits)).isEqualTo(2);
        assertThat(Commit.countDeletes(commits)).isZero();
        assertThat(Commit.countMoves(commits)).isZero();
        assertThat(Commit.countCommits(commits)).isEqualTo(2);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 2 commits analyzed",
                "-> 2 MODIFY commits",
                "-> 4 lines added",
                "-> 6 lines added");

        Commit third = new Commit("2", AUTHOR, 2);
        third.setNewPath(Commit.NO_FILE_NAME);
        third.setOldPath("old");
        commits.add(third);

        assertThat(Commit.countAddedLines(commits)).isEqualTo(1 + 3);
        assertThat(Commit.countDeletedLines(commits)).isEqualTo(2 + 4);
        assertThat(Commit.countAuthors(commits)).isEqualTo(2);
        assertThat(Commit.countChanges(commits)).isEqualTo(2);
        assertThat(Commit.countDeletes(commits)).isEqualTo(1);
        assertThat(Commit.countMoves(commits)).isZero();
        assertThat(Commit.countCommits(commits)).isEqualTo(2);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 2 commits analyzed",
                "-> 2 MODIFY commits",
                "-> 1 DELETE commits",
                "-> 4 lines added",
                "-> 6 lines added");

        Commit forth = new Commit("3", AUTHOR, 3);
        forth.setNewPath("new");
        forth.setOldPath("old");
        commits.add(forth);

        assertThat(Commit.countAddedLines(commits)).isEqualTo(1 + 3);
        assertThat(Commit.countDeletedLines(commits)).isEqualTo(2 + 4);
        assertThat(Commit.countAuthors(commits)).isEqualTo(2);
        assertThat(Commit.countChanges(commits)).isEqualTo(2);
        assertThat(Commit.countDeletes(commits)).isEqualTo(1);
        assertThat(Commit.countMoves(commits)).isEqualTo(1);
        assertThat(Commit.countCommits(commits)).isEqualTo(3);

        assertThat(logCommits(commits).getInfoMessages()).containsExactly(
                "-> 3 commits analyzed",
                "-> 2 MODIFY commits",
                "-> 1 RENAME commits",
                "-> 1 DELETE commits",
                "-> 4 lines added",
                "-> 6 lines added");
    }

    private FilteredLog logCommits(final List<Commit> commits) {
        FilteredLog log = new FilteredLog("Error");
        Commit.logCommits(commits, log);
        return log;
    }

    @Override
    protected Commit createSerializable() {
        return new Commit(ID, AUTHOR, COMMITTED_AT).addLines(5).deleteLines(8).setNewPath("file.txt");
    }
}
