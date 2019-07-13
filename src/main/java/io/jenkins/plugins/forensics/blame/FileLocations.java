package io.jenkins.plugins.forensics.blame;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;

import io.jenkins.plugins.forensics.util.FilteredLog;

/**
 * Defines a set of file locations. A file location is identified by the file name (relative to the workspace location)
 * and line number. File locations are grouped by file name, i.e. you can obtain a mapping of a file to all affected
 * lines in that file.
 *
 * @author Ullrich Hafner
 */
public class FileLocations extends FilteredLog {
    private static final long serialVersionUID = 8063580789984061223L;

    private static final String UNIX_SLASH = "/";
    private static final String WINDOWS_BACK_SLASH = "\\";

    private final Set<String> skippedFiles = new HashSet<>();
    private final Map<String, Set<Integer>> linesPerFile = new HashMap<>();

    private final String workspace;

    /**
     * Creates a new instance of {@link FileLocations}.
     *
     * @param workspace
     *         the workspace to get the Git repository from
     */
    public FileLocations(final String workspace) {
        this(workspace, new FileSystem());
    }

    /**
     * Creates a new instance of {@link FileLocations}.
     *
     * @param workspace
     *         the workspace to get the Git repository from
     */
    public FileLocations(final File workspace) {
        this(workspace.getPath());
    }

    /**
     * Creates a new instance of {@link FileLocations}.
     *
     * @param workspace
     *         the workspace to get the Git repository from
     * @param fileSystem
     *         file system stub
     */
    @VisibleForTesting
    public FileLocations(final String workspace, final FileSystem fileSystem) {
        super("Errors while marking lines in affected lines:");

        this.workspace = normalizeWorkspace(fileSystem, workspace) + UNIX_SLASH;
    }

    private String normalizeWorkspace(final FileSystem fileSystem, final String platformFileName) {
        String absolutePath = fileSystem.resolveAbsolutePath(platformFileName, this);
        String clean = StringUtils.replace(StringUtils.strip(absolutePath), WINDOWS_BACK_SLASH, UNIX_SLASH);

        return StringUtils.removeEnd(clean, UNIX_SLASH);
    }

    @VisibleForTesting
    public String getWorkspace() {
        return workspace;
    }

    public Set<String> getSkippedFiles() {
        return skippedFiles;
    }

    /**
     * Adds the specified affected file and line number. This file and line will be processed by Git blame later on.
     * Only files that are part of the {@link #workspace} will be processed.
     *
     * @param fileName
     *         the absolute file name that will be used as a key
     * @param lineStart
     *         the line number to find the blame for
     */
    public void addLine(final String fileName, final int lineStart) {
        if (isPartOfWorkspace(fileName)) {
            linesPerFile.put(removeWorkspace(fileName), mergeLine(fileName, lineStart));
        }
        else {
            skippedFiles.add(fileName);
        }
    }

    private Set<Integer> mergeLine(final String fileName, final int lineStart) {
        Set<Integer> lines;
        if (contains(fileName)) {
            lines = getLines(fileName);
        }
        else {
            lines = new HashSet<>();
        }
        lines.add(lineStart);
        return lines;
    }

    private boolean isPartOfWorkspace(final String fileName) {
        return StringUtils.startsWith(fileName, workspace);
    }

    /**
     * Returns whether some files have been added.
     *
     * @return {@code true} if there a no blames available, {@code false} otherwise
     */
    public boolean isEmpty() {
        return linesPerFile.isEmpty();
    }

    /**
     * Returns the number of files that have been added.
     *
     * @return number of affected files with blames
     */
    @Override
    public int size() {
        return linesPerFile.keySet().size();
    }

    /**
     * Returns whether the specified file has been added.
     *
     * @param fileName
     *         the relative or absolute path of the file
     *
     * @return {@code true} if the file already has been added, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return containsFile(fileName) || containsFile(removeWorkspace(fileName));
    }

    private boolean containsFile(final String fileName) {
        return linesPerFile.containsKey(fileName);
    }

    private String removeWorkspace(final String fileName) {
        return StringUtils.removeStart(fileName, workspace);
    }

    /**
     * Returns the relative file names of all files.
     *
     * @return the relative file names
     */
    public Set<String> getRelativePaths() {
        return new HashSet<>(linesPerFile.keySet());
    }

    /**
     * Returns the absolute file names of all files.
     *
     * @return the absolute file names
     */
    public Set<String> getAbsolutePaths() {
        return linesPerFile.keySet().stream().map(this::createAbsolutePath).collect(Collectors.toSet());
    }

    /**
     * Returns the added lines for the specified file.
     *
     * @param fileName
     *         the relative or absolute path of the file
     *
     * @return the lines for that file
     * @throws NoSuchElementException
     *         if the file name is not registered
     */
    public Set<Integer> getLines(final String fileName) {
        if (containsFile(fileName)) {
            return linesPerFile.get(fileName);
        }
        String relativePath = removeWorkspace(fileName);
        if (containsFile(relativePath)) {
            return linesPerFile.get(relativePath);
        }
        throw new NoSuchElementException(String.format("No information for file '%s' stored", fileName));
    }

    private String createAbsolutePath(final String relativePath) {
        return workspace + relativePath;
    }

    /**
     * File system facade for test cases.
     */
    @VisibleForTesting
    public static class FileSystem {
        /**
         * Resolves the absolute path for a file.
         *
         * @param fileName
         *         the relative file name to resolve
         * @param log
         *         a logger
         *
         * @return the absolute path (or the original {@code fileName} if the file name could not be resolved
         */
        public String resolveAbsolutePath(final String fileName, final FilteredLog log) {
            try {
                return Paths.get(fileName)
                        .toAbsolutePath()
                        .normalize()
                        .toRealPath(LinkOption.NOFOLLOW_LINKS)
                        .toString();
            }
            catch (IOException | InvalidPathException exception) {
                log.logException(exception, "Can't resolve absolute workspace path '%s'", fileName);

                return fileName;
            }
        }
    }
}
