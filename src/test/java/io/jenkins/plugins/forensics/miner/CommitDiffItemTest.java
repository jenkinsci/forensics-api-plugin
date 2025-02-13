package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link CommitDiffItem}.
 *
 * @author Ullrich Hafner
 */
class CommitDiffItemTest extends SerializableTest<CommitDiffItem> {
    private static final TreeStringBuilder BUILDER = new TreeStringBuilder();
    private static final String ID = "ID";
    private static final String AUTHOR = "author";
    private static final int COMMITTED_AT = 1;

    @Test
    void shouldCreateEmptyCommit() {
        var commit = new CommitDiffItem(ID, AUTHOR, COMMITTED_AT);

        verifyEmptyCommit(commit);

        var another = new CommitDiffItem(commit);

        verifyEmptyCommit(another);
    }

    private void verifyEmptyCommit(final CommitDiffItem commit) {
        assertThat(commit).hasId(ID)
                .hasAuthor(AUTHOR)
                .hasTime(COMMITTED_AT)
                .hasTotalAddedLines(0)
                .hasTotalDeletedLines(0)
                .hasNewPath(CommitDiffItem.NO_FILE_NAME)
                .hasOldPath(CommitDiffItem.NO_FILE_NAME)
                .isNotDelete()
                .isNotMove();
    }

    @Test
    void shouldChangeLines() {
        var commit = new CommitDiffItem(ID, AUTHOR, COMMITTED_AT);

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
        var commit = new CommitDiffItem(ID, AUTHOR, COMMITTED_AT);

        commit.setNewPath(asTreeString("new"));

        assertThat(commit)
                .hasNewPath("new")
                .hasOldPath(CommitDiffItem.NO_FILE_NAME)
                .isNotMove()
                .isNotDelete();

        commit.setOldPath(asTreeString("old"));
        assertThat(commit)
                .hasNewPath("new")
                .hasOldPath("old")
                .isMove()
                .isNotDelete();

        commit.setNewPath(asTreeString(CommitDiffItem.NO_FILE_NAME));
        assertThat(commit)
                .hasNewPath(CommitDiffItem.NO_FILE_NAME)
                .hasOldPath("old")
                .isNotMove()
                .isDelete();
    }

    private TreeString asTreeString(final String old) {
        return BUILDER.intern(old);
    }

    @Override
    protected CommitDiffItem createSerializable() {
        return new CommitDiffItem(ID, AUTHOR, COMMITTED_AT)
                .addLines(5)
                .deleteLines(8)
                .setNewPath(asTreeString("file.txt"));
    }
}
