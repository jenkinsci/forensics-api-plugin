package io.jenkins.plugins.forensics.delta;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Data class that represents the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
public class Delta implements Serializable {
    @Serial
    private static final long serialVersionUID = 5641235877389921937L;

    static final String ERROR_MESSAGE_UNKNOWN_FILE = "No information about changes for the file with the ID '%s' stored";

    private final String currentCommit;
    private final String referenceCommit;

    /** Contains the changes for modified files, mapped by the file ID. */
    @SuppressWarnings("PMD.LooseCoupling")
    private final HashMap<String, FileChanges> fileChangesMap;

    /**
     * Constructor for a delta instance which wraps code changes between the two passed commits.
     *
     * @param currentCommit
     *         The currently processed commit
     * @param referenceCommit
     *         The reference commit
     * @param fileChangesMap
     *         The map which contains the changes for modified files, mapped by the file ID.
     */
    public Delta(final String currentCommit, final String referenceCommit,
            final Map<String, FileChanges> fileChangesMap) {
        this.currentCommit = currentCommit;
        this.referenceCommit = referenceCommit;
        this.fileChangesMap = new HashMap<>(fileChangesMap);
    }

    public String getCurrentCommit() {
        return currentCommit;
    }

    public String getReferenceCommit() {
        return referenceCommit;
    }

    public Map<String, FileChanges> getFileChangesMap() {
        return new HashMap<>(fileChangesMap);
    }

    /**
     * Returns information about changes made to the specified file.
     *
     * @param fileId
     *         the ID of the file
     *
     * @return the information about changes made to the specified file
     * @throws NoSuchElementException
     *         if the file ID is not registered
     */
    public FileChanges getFileChangesById(final String fileId) {
        if (fileChangesMap.containsKey(fileId)) {
            return fileChangesMap.get(fileId);
        }
        throw new NoSuchElementException(ERROR_MESSAGE_UNKNOWN_FILE.formatted(fileId));
    }

    /**
     * Adds information about changes made to the specified file.
     *
     * <p>If there are already information about changes to the specified file, the old information will be overwritten by
     * the new one.
     *
     * @param fileId
     *         The ID of the file
     * @param fileChange
     *         Information about the made changes
     */
    public void addFileChanges(final String fileId, final FileChanges fileChange) {
        fileChangesMap.put(fileId, fileChange);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var delta = (Delta) o;
        return Objects.equals(currentCommit, delta.currentCommit)
                && Objects.equals(referenceCommit, delta.referenceCommit)
                && Objects.equals(fileChangesMap, delta.fileChangesMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCommit, referenceCommit, fileChangesMap);
    }
}
