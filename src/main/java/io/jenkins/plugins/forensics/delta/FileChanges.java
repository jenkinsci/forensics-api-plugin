package io.jenkins.plugins.forensics.delta;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps all changes made to one specific file.
 *
 * @author Florian Orendi
 */
public class FileChanges implements Serializable {
    @Serial
    private static final long serialVersionUID = 6135245877389921937L;

    private final String fileName;
    private final String oldFileName;

    private final String fileContent;

    /**
     * The {@link FileEditType} describing how the file has been affected.
     */
    private final FileEditType fileEditType;

    /**
     * A map changes made to the file, mapped by the {@link ChangeEditType}.
     */
    private final Map<ChangeEditType, Set<Change>> changes;

    /**
     * Constructor for an instance which wraps all changes made to a specific file.
     *
     * @param fileName
     *         The name of the file
     * @param oldFileName
     *         The old file name before the edit
     * @param fileContent
     *         The content of the file
     * @param fileEditType
     *         The change type how the file has been affected
     * @param changes
     *         The changes made to the file
     */
    public FileChanges(final String fileName, final String oldFileName, final String fileContent,
            final FileEditType fileEditType, final Map<ChangeEditType, Set<Change>> changes) {
        this.fileName = fileName;
        this.oldFileName = oldFileName;
        this.fileContent = fileContent;
        this.fileEditType = fileEditType;
        this.changes = createMap(changes);
    }

    private Map<ChangeEditType, Set<Change>> createMap(final Map<ChangeEditType, Set<Change>> existingChanges) {
        if (existingChanges.isEmpty()) {
            return new EnumMap<>(ChangeEditType.class);
        }
        return new EnumMap<>(existingChanges);
    }

    public String getFileName() {
        return fileName;
    }

    public String getOldFileName() {
        return oldFileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public FileEditType getFileEditType() {
        return fileEditType;
    }

    public Map<ChangeEditType, Set<Change>> getChanges() {
        return createMap(changes);
    }

    /**
     * Returns information about changes of a specified type.
     *
     * @param changeEditType
     *         The edit type
     *
     * @return the information about changes of the specified type
     */
    public Set<Change> getChangesByType(final ChangeEditType changeEditType) {
        if (changes.containsKey(changeEditType)) {
            return changes.get(changeEditType);
        }
        return new HashSet<>();
    }

    /**
     * Adds information about a change and stores it according to the type of edit.
     *
     * @param change
     *         The change to be stored
     */
    public void addChange(final Change change) {
        var changeEditType = change.getEditType();
        if (changes.containsKey(changeEditType)) {
            changes.get(changeEditType).add(change);
        }
        else {
            changes.put(change.getEditType(), Stream.of(change).collect(Collectors.toSet()));
        }
    }

    /**
     * Returns all modified lines in this changed file.
     *
     * @return the modified line
     */
    public Set<Integer> getModifiedLines() {
        return changes.values().stream()
                .flatMap(Collection::stream)
                .map(this::getModifiedLinesForChange)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getModifiedLinesForChange(final Change filter) {
        if (filter.getEditType() == ChangeEditType.INSERT || filter.getEditType() == ChangeEditType.REPLACE) {
            var lines = new HashSet<Integer>();
            for (int line = filter.getFromLine(); line <= filter.getToLine(); line++) {
                lines.add(line);
            }
            return lines;
        }
        return Set.of();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (FileChanges) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(oldFileName, that.oldFileName)
                && Objects.equals(fileContent, that.fileContent)
                && fileEditType == that.fileEditType
                && Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, oldFileName, fileContent, fileEditType, changes);
    }
}
