package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * A change made on specific lines within a specific file.
 * <p>
 * The lines are defined by a starting and an ending point (1-based line counter), describing the made changes
 * within the new version of the file. In case of a deleted file, the line range describes the deleted lines within the
 * old version of the file, since only then it is possible to determine what has been deleted.
 *
 * @author Florian Orendi
 */
@SuppressWarnings("PMD.DataClass")
public class Change implements Serializable {

    private static final long serialVersionUID = 1543635877389921937L;

    /**
     * The {@link ChangeEditType} of the change.
     */
    private final ChangeEditType changeEditType;

    /**
     * The starting point of the change (1-based).
     */
    private final int fromLine;
    /**
     * The ending point of the change (1-based).
     */
    private final int toLine;

    /**
     * Constructor for an instance which wraps a specific change within a file.
     *
     * @param changeEditType The type of the change
     * @param fromLine       The starting line
     * @param toLine         The ending line
     */
    public Change(final ChangeEditType changeEditType, final int fromLine, final int toLine) {
        this.changeEditType = changeEditType;
        this.fromLine = fromLine;
        this.toLine = toLine;
    }

    public ChangeEditType getEditType() {
        return changeEditType;
    }

    public int getFromLine() {
        return fromLine;
    }

    public int getToLine() {
        return toLine;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Change change = (Change) o;
        return fromLine == change.fromLine
                && toLine == change.toLine
                && changeEditType == change.changeEditType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeEditType, fromLine, toLine);
    }
}
