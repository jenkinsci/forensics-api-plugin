package io.jenkins.plugins.forensics.miner;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link Commit}.
 *
 * @author Ullrich Hafner
 */
class CommitTest {
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
    void shouldSortByCommitTimeDescending() {
        Commit first = new Commit(ID, AUTHOR, 0);
        Commit second = new Commit(ID, AUTHOR, 1);
        Commit third = new Commit(ID, AUTHOR, 2);

        Commit[] commits = {first, second, third};
        Arrays.sort(commits);

        assertThat(commits).containsExactly(third, second, first);
    }
}
