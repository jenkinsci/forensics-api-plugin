package io.jenkins.plugins.forensics.blame;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Provides access to the blame information for a collection of workspace files. File names must use absolute paths.
 * Additionally, info and error messages during the SCM processing can be stored.
 *
 * @author Ullrich Hafner
 */
public class Blames implements Serializable {
    private static final long serialVersionUID = -1192940891942480612L;

    private final Map<String, FileBlame> blamesPerFile = new HashMap<>();

    /**
     * Adds the specified blame to this collection of blames.
     *
     * @param additionalBlame
     *         the blame to add
     */
    public void add(final FileBlame additionalBlame) {
        merge(additionalBlame.getFileName(), additionalBlame);
    }

    /**
     * Merges all specified blames with the current collection of blames.
     *
     * @param other
     *         the blames to add
     */
    public void addAll(final Blames other) {
        for (String otherFile : other.getFiles()) {
            FileBlame otherRequest = other.getBlame(otherFile);
            merge(otherFile, otherRequest);
        }
    }

    private void merge(final String otherFile, final FileBlame otherRequest) {
        if (contains(otherFile)) {
            getBlame(otherFile).merge(otherRequest);
        }
        else {
            blamesPerFile.put(otherFile, otherRequest);
        }
    }

    /**
     * Returns whether there are files with blames.
     *
     * @return {@code true} if there a no blames available, {@code false} otherwise
     */
    public boolean isEmpty() {
        return blamesPerFile.isEmpty();
    }

    /**
     * Returns the number of files with blames.
     *
     * @return number of affected files with blames
     */
    public int size() {
        return blamesPerFile.keySet().size();
    }

    /**
     * Returns whether there are blames for the specified file.
     *
     * @param fileName
     *         the relative or absolute path of the file
     *
     * @return {@code true} if the file already has been added, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return blamesPerFile.containsKey(fileName);
    }

    /**
     * Returns all files with blames.
     *
     * @return the files with blames
     */
    public Set<String> getFiles() {
        return new HashSet<>(blamesPerFile.keySet());
    }

    /**
     * Returns the blame information for the specified file.
     *
     * @param fileName
     *         the absolute path of the file
     *
     * @return the blame information for the specified file.
     * @throws NoSuchElementException
     *         if the file name is not registered
     */
    public FileBlame getBlame(final String fileName) {
        if (blamesPerFile.containsKey(fileName)) {
            return blamesPerFile.get(fileName);
        }
        throw new NoSuchElementException(String.format("No blame information for file '%s' stored", fileName));
    }
}
