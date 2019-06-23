package io.jenkins.plugins.forensics.blame;

import java.io.Serializable;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.multimap.set.UnifiedSetMultimap;

import com.google.common.annotations.VisibleForTesting;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Defines a set of file locations. A file location is identified by the file name (relative to the workspace location)
 * and line number. File locations are grouped by file name, i.e. you can obtain a mapping of file to all affected lines
 * in that file.
 *
 * @author Ullrich Hafner
 */
public class FileLocations implements Serializable {
    private static final long serialVersionUID = -7884822502506035784L;

    private final MutableMultimap<String, Integer> locationsPerFile = new UnifiedSetMultimap<>();
    private final Set<String> skippedFiles = new HashSet<>();
    private final String workspace;

    /**
     * Creates an empty instance of {@link FileLocations}.
     */
    @VisibleForTesting
    public FileLocations() {
        this(StringUtils.EMPTY);
    }

    /**
     * Creates an empty instance of {@link FileLocations} that will work on the specified workspace.
     *
     * @param workspace
     *         the workspace to get the Git repository from
     */
    public FileLocations(final String workspace) {
        this.workspace = normalizeFileName(workspace);
    }

    private String normalizeFileName(@Nullable final String platformFileName) {
        return StringUtils.replace(StringUtils.strip(platformFileName), "\\", "/");
    }

    /**
     * Returns whether there are files with blames in this instance.
     *
     * @return {@code true} if there a no blames available, {@code false} otherwise
     */
    public boolean isEmpty() {
        return locationsPerFile.isEmpty();
    }

    /**
     * Returns the number of files that have been added to this instance.
     *
     * @return number of affected files with blames
     */
    public int size() {
        return locationsPerFile.keySet().size();
    }

    /**
     * Returns whether the specified file already has been added.
     *
     * @param fileName
     *         the name of the file
     *
     * @return {@code true} if the file already has been added, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return locationsPerFile.containsKey(fileName);
    }

    public Set<String> getSkippedFiles() {
        return skippedFiles;
    }

    /**
     * Adds a blame request for the specified affected file and line number. This file and line will be processed by Git
     * blame later on.
     *
     * @param fileName
     *         the absolute file name that will be used as a key
     * @param lineStart
     *         the line number to find the blame for
     */
    public void addLine(final String fileName, final int lineStart) {
        if (fileName.startsWith(workspace)) {
            String relativeFileName = fileName.substring(workspace.length());
            String cleanFileName = StringUtils.removeStart(relativeFileName, "/");
            locationsPerFile.put(cleanFileName, lineStart);
        }
        else {
            skippedFiles.add(fileName);
        }
    }

    /**
     * Returns the absolute file names of the affected files that will be processed by Git blame.
     *
     * @return the file names
     */
    public Set<String> getFiles() {
        return locationsPerFile.keySet().toSet();
    }

    /**
     * Returns the blames for the specified file.
     *
     * @param fileName
     *         absolute file name
     *
     * @return the blames for that file
     * @throws NoSuchElementException
     *         if the file name is not registered
     */
    public Set<Integer> get(final String fileName) {
        if (contains(fileName)) {
            return locationsPerFile.get(fileName).toSet();
        }
        throw new NoSuchElementException(String.format("No information for file %s stored", fileName));
    }
}
