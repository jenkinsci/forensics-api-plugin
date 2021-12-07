package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Models the code difference - so called 'delta' - between two commits.
 *
 * @author Florian Orendi
 */
public class Delta implements Serializable {

    private static final long serialVersionUID = 5641235877389921937L;

    /**
     * The currently processed commit.
     */
    private final String currentCommit;

    /**
     * The reference commit.
     */
    private final String referenceCommit;

    /**
     * The difference file created by Git.
     */
    private String diffFile;

    /**
     * Map which contains the changes for modified files, mapped by the file ID.
     */
    private Map<String, FileChanges> fileChanges;

    /**
     * Constructor for a delta instance which wraps code changes between the two passed commits.
     *
     * @param currentCommit   The currently processed commit
     * @param referenceCommit The reference commit
     */
    public Delta(final String currentCommit, final String referenceCommit) {
        this.currentCommit = currentCommit;
        this.referenceCommit = referenceCommit;
        this.diffFile = "";
        this.fileChanges = new HashMap<>();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCurrentCommit() {
        return currentCommit;
    }

    public String getReferenceCommit() {
        return referenceCommit;
    }

    public String getDiffFile() {
        return diffFile;
    }

    public void setDiffFile(final String diffFile) {
        this.diffFile = diffFile;
    }

    public Map<String, FileChanges> getFileChanges() {
        return fileChanges;
    }

    public void setFileChanges(final Map<String, FileChanges> fileChanges) {
        this.fileChanges = fileChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delta delta = (Delta) o;
        return Objects.equals(currentCommit, delta.currentCommit)
                && Objects.equals(referenceCommit, delta.referenceCommit)
                && Objects.equals(diffFile, delta.diffFile)
                && Objects.equals(fileChanges, delta.fileChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCommit, referenceCommit, diffFile, fileChanges);
    }
}
