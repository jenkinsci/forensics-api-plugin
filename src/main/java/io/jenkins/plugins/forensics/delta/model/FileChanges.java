package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps all changes made to one specific file.
 *
 * @author Florian Orendi
 */
public class FileChanges implements Serializable {

    private static final long serialVersionUID = 6135245877389921937L;

    /**
     * The file name.
     */
    private final String fileName;

    /**
     * The new file content.
     */
    private final String fileContent;

    /**
     * The {@link FileEditType} describing how the file has been affected.
     */
    private final FileEditType fileEditType;

    /**
     * A map changes made to the file, mapped by the {@link ChangeEditType}.
     */
    private final Map<ChangeEditType, List<Change>> changes;

    public FileChanges(final String fileName, final String fileContent, final FileEditType fileEditType,
            final Map<ChangeEditType, List<Change>> changes) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.fileEditType = fileEditType;
        this.changes = changes;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public FileEditType getFileEditType() {
        return fileEditType;
    }

    public Map<ChangeEditType, List<Change>> getChanges() {
        return new HashMap<>(changes);
    }
}
