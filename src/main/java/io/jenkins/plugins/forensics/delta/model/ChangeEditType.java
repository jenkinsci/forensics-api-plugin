package io.jenkins.plugins.forensics.delta.model;

/**
 * The edit type of a single change within a specific file.
 *
 * @author Florian Orendi
 */
public enum ChangeEditType {
    /**
     * The new content replaces old content.
     */
    REPLACE,
    /**
     * New content has been added.
     */
    INSERT,
    /**
     * Content has been deleted.
     */
    DELETE,
    /**
     * Nothing happened with the content.
     */
    EMPTY,
    /**
     * The edit type could not be determined.
     */
    UNDEFINED
}
