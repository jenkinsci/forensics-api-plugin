package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides access to the SCM statistics of all repository files. Additionally,
 * info and error messages during the SCM processing will be stored.
 *
 * @author Ullrich Hafner
 */
public class RepositoryStatistics implements Serializable {
    private static final long serialVersionUID = 7L; // release 0.7

    private final Map<String, FileStatistics> statisticsPerFile = new HashMap<>();
    private final long currentTimestamp;
    private long totalRuntime;

    private void updateTotalRuntime(){
        totalRuntime = 1 + (System.nanoTime() - currentTimestamp) / 1_000_000_000L ;
    }

    RepositoryStatistics(){
        currentTimestamp = System.nanoTime();
    }

    /**
     * Returns whether the repository is empty.
     *
     * @return {@code true} if the repository is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return statisticsPerFile.isEmpty();
    }

    /**
     * Returns the number of files in the repository.
     *
     * @return number of files in the repository
     */
    public int size() {
        return statisticsPerFile.size();
    }

    /**
     * Returns whether the specified file is part of the repository.
     *
     * @param fileName
     *         the name of the file
     *
     * @return {@code true} if the file file is part of the repository, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return statisticsPerFile.containsKey(fileName);
    }

    /**
     * Returns the absolute file names of the files that are part of the repository.
     *
     * @return the file names
     */
    public Set<String> getFiles() {
        return statisticsPerFile.keySet();
    }

    /**
     * Returns the statistics for all repository files.
     *
     * @return the requests
     */
    public Collection<FileStatistics> getFileStatistics() {
        return statisticsPerFile.values();
    }

    /**
     * Returns the statistics for the specified file.
     *
     * @param fileName
     *         absolute file name
     *
     * @return the statistics for that file
     * @throws NoSuchElementException
     *         if the file name is not registered
     */
    public FileStatistics get(final String fileName) {
        if (contains(fileName)) {
            return statisticsPerFile.get(fileName);
        }
        throw new NoSuchElementException(String.format("No information for file %s stored", fileName));
    }

    /**
     * Adds all additional file statistics.
     *
     * @param additionalStatistics
     *         the additional statistics to add
     */
    public void addAll(final Collection<FileStatistics> additionalStatistics) {
        statisticsPerFile.putAll(
                additionalStatistics.stream().collect(Collectors.toMap(FileStatistics::getFileName, Function.identity())));
        updateTotalRuntime();
    }

    /**
     * Adds all additional file statistics.
     *
     * @param additionalStatistics
     *         the additional statistics to add
     */
    public void addAll(final RepositoryStatistics additionalStatistics) {
        addAll(additionalStatistics.getFileStatistics());
    }

    /**
     * Adds the additional file statistics instance.
     *
     * @param additionalStatistics
     *         the additional statistics to add
     */
    public void add(final FileStatistics additionalStatistics) {
        statisticsPerFile.put(additionalStatistics.getFileName(), additionalStatistics);
        updateTotalRuntime();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepositoryStatistics that = (RepositoryStatistics) o;
        return statisticsPerFile.equals(that.statisticsPerFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statisticsPerFile);
    }

    public long getTotalRuntime(){
        return totalRuntime;
    }

}
