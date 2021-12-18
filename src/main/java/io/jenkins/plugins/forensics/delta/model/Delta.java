package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Data class that represents the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
@SuppressWarnings("PMD.DataClass")
public class Delta implements Serializable {

    private static final long serialVersionUID = 5641235877389921937L;

    private final String currentCommit;

    private final String referenceCommit;

    /**
     * Map which contains the changes for modified files, mapped by the file ID.
     */
    private final Map<String, FileChanges> fileChanges;

    /**
     * Constructor for a delta instance which wraps code changes between the two passed commits.
     *
     * @param currentCommit
     *         The currently processed commit
     * @param referenceCommit
     *         The reference commit
     * @param fileChanges
     *         The map which contains the changes for modified files, mapped by the file ID.
     */
    public Delta(final String currentCommit, final String referenceCommit, final Map<String, FileChanges> fileChanges) {
        this.currentCommit = currentCommit;
        this.referenceCommit = referenceCommit;
        this.fileChanges = Collections.unmodifiableMap(fileChanges);
    }

    public String getCurrentCommit() {
        return currentCommit;
    }

    public String getReferenceCommit() {
        return referenceCommit;
    }

    public Map<String, FileChanges> getFileChanges() {
        return fileChanges;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Delta delta = (Delta) o;
        return Objects.equals(currentCommit, delta.currentCommit)
                && Objects.equals(referenceCommit, delta.referenceCommit)
                && Objects.equals(fileChanges, delta.fileChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCommit, referenceCommit, fileChanges);
    }
}
