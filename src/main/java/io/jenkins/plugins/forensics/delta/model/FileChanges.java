package io.jenkins.plugins.forensics.delta.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Wraps all changes made to one specific file.
 *
 * @author Florian Orendi
 */
@SuppressWarnings("PMD.DataClass")
public class FileChanges implements Serializable {

    private static final long serialVersionUID = 6135245877389921937L;

    private final String fileName;

    private final String fileContent;

    /**
     * The {@link FileEditType} describing how the file has been affected.
     */
    private final FileEditType fileEditType;

    /**
     * A map changes made to the file, mapped by the {@link ChangeEditType}.
     */
    private final Map<ChangeEditType, List<Change>> changes;

    /**
     * Constructor for an instance which wraps all changes made to a specific file.
     *
     * @param fileName
     *         The name of the file
     * @param fileContent
     *         The content of the file
     * @param fileEditType
     *         The change type how the file has been affected
     * @param changes
     *         The changes made to the file
     */
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
        return changes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileChanges that = (FileChanges) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(fileContent, that.fileContent)
                && fileEditType == that.fileEditType
                && Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileContent, fileEditType, changes);
    }
}
