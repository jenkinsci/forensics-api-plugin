package io.jenkins.plugins.forensics.delta.model;

/**
 * Edit types which describe how a file has been changed.
 *
 * @author Florian Orendi
 */
public enum FileEditType {
    /**
     * The file has been added.
     */
    ADD,
    /**
     * The file has been modified.
     */
    MODIFY,
    /**
     * The file has been deleted.
     */
    DELETE,
    /**
     * The file has been renamed.
     */
    RENAME,
    /**
     * The file has been copied.
     */
    COPY,
    /**
     * The edit type could not be determined.
     */
    UNDEFINED
}
