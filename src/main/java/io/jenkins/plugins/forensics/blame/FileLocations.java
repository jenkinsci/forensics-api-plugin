package io.jenkins.plugins.forensics.blame;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Defines a set of file locations. A file location is identified by an absolute file name and line number. File
 * locations are grouped by file name, i.e. you can obtain a mapping of a file to all affected lines in that file.
 *
 * @author Ullrich Hafner
 */
public class FileLocations implements Serializable {
    @Serial
    private static final long serialVersionUID = 8063580789984061223L;

    @SuppressWarnings("PMD.LooseCoupling")
    private final HashMap<String, Set<Integer>> linesPerFile = new HashMap<>();

    /**
     * Adds the specified affected file and line number.
     *
     * @param fileName
     *         the absolute file name that will be used as a key
     * @param lineStart
     *         the line number to find the blame for
     */
    public void addLine(final String fileName, final int lineStart) {
        linesPerFile.put(fileName, mergeLine(fileName, lineStart));
    }

    private Set<Integer> mergeLine(final String fileName, final int lineStart) {
        Set<Integer> lines;
        if (contains(fileName)) {
            lines = linesPerFile.get(fileName);
        }
        else {
            lines = new HashSet<>();
        }
        lines.add(lineStart);
        return lines;
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
    public int size() {
        return linesPerFile.size();
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
        return containsFile(fileName);
    }

    private boolean containsFile(final String fileName) {
        return linesPerFile.containsKey(fileName);
    }

    /**
     * Returns the absolute file names of all files.
     *
     * @return the absolute file names
     */
    public Set<String> getFiles() {
        return Collections.unmodifiableSet(linesPerFile.keySet());
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
            return Collections.unmodifiableSet(linesPerFile.get(fileName));
        }
        throw new NoSuchElementException("No information for file '%s' stored".formatted(fileName));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (FileLocations) o;
        return linesPerFile.equals(that.linesPerFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linesPerFile);
    }
}
