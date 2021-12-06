package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
}
