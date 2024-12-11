package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import edu.hm.hafner.util.Generated;
import edu.hm.hafner.util.TreeString;

/**
 * Represents all changes related to a specific file in a given SCM commit (diff). For each commit the number of added
 * and deleted lines will be recorded. Since a commit consists of a list of differences the number of added or deleted
 * lines can be updated several times until the final size has been reached.
 *
 * @author Ullrich Hafner
 */
public class CommitDiffItem implements Serializable {
    private static final long serialVersionUID = 1L; // since 0.8.0

    /** Indicates that a file name has not been set or a file has been deleted. */
    static final String NO_FILE_NAME = "/dev/null";
    private static final TreeString NO_FILE_AS_TREE_STRING = TreeString.valueOf(NO_FILE_NAME);

    private String id;
    private String author;
    private final int time;

    private int totalAddedLines = 0;
    private int totalDeletedLines = 0;

    private TreeString oldPath = NO_FILE_AS_TREE_STRING;
    private TreeString newPath = NO_FILE_AS_TREE_STRING;

    /**
     * Creates a new {@link CommitDiffItem}.
     *
     * @param id
     *         commit ID
     * @param author
     *         author of the commit
     * @param time
     *         the time of the commit (given as number of seconds since the standard base time known as "the epoch",
     *         namely January 1, 1970, 00:00:00 GMT)
     */
    public CommitDiffItem(final String id, final String author, final int time) {
        this.id = id.intern();
        this.author = author.intern();
        this.time = time;
    }

    /**
     * Called after deserialization to improve the memory usage.
     *
     * @return this
     */
    protected Object readResolve() {
        id = id.intern();
        author = author.intern();

        return this;
    }

    /**
     * Creates a new copy of the specified {@link CommitDiffItem}. Note that the number of added and deleted lines will
     * be set to zero, so this constructor is not a copy constructor in the common sense.
     *
     * @param copy
     *         the commit to copy the base properties from
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public CommitDiffItem(final CommitDiffItem copy) {
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
        return oldPath.toString();
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
        return newPath.toString();
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
    public CommitDiffItem addLines(final int addedLines) {
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
    public CommitDiffItem deleteLines(final int deletedLines) {
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
    public CommitDiffItem setOldPath(final TreeString oldPath) {
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
    public CommitDiffItem setNewPath(final TreeString newPath) {
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
        CommitDiffItem commit = (CommitDiffItem) o;
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
        return new StringJoiner(", ", CommitDiffItem.class.getSimpleName() + "[", "]")
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
