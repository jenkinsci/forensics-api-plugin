package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides access to the SCM commit statistics of all repository files up to a specific commit.
 *
 * @author Ullrich Hafner
 */
public class RepositoryStatistics implements Serializable {
    private static final long serialVersionUID = 8L; // release 0.8

    private final Map<String, FileStatistics> statisticsPerFile = new HashMap<>();

    private String latestCommitId;
    private int totalLinesOfCode;
    private int totalChurn;

    /**
     * Creates an empty instance of {@link RepositoryStatistics} with no latest commit ID set.
     */
    public RepositoryStatistics() {
        this(StringUtils.EMPTY);
    }

    /**
     * Creates an empty instance of {@link RepositoryStatistics}.
     *
     * @param latestCommitId
     *         the ID of the latest commit that
     */
    public RepositoryStatistics(final String latestCommitId) {
        this.latestCommitId = latestCommitId;
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (latestCommitId == null) {
            latestCommitId = StringUtils.EMPTY;
        }
        return this;
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
     * Returns the ID of the latest commit mined.
     *
     * @return ID of the latest commit.
     */
    public String getLatestCommitId() {
        return latestCommitId;
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
        return Collections.unmodifiableSet(statisticsPerFile.keySet());
    }

    /**
     * Returns the statistics for all repository files.
     *
     * @return the statistics
     */
    public Collection<FileStatistics> getFileStatistics() {
        return Collections.unmodifiableCollection(statisticsPerFile.values());
    }

    /**
     * Returns the mapping of file names to statistics.
     *
     * @return the mapping of file names to statistics
     */
    public Map<String, FileStatistics> getMapping() {
        return Collections.unmodifiableMap(statisticsPerFile);
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
                additionalStatistics.stream()
                        .collect(Collectors.toMap(FileStatistics::getFileName, Function.identity())));
        calculateTotalLinesOfCode();
        calculateTotalChurn();
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

    private void calculateTotalLinesOfCode() {
        statisticsPerFile.forEach((k, v) -> totalLinesOfCode += v.getLinesOfCode());
    }

    private void calculateTotalChurn() {
        statisticsPerFile.forEach((k, v) -> totalChurn += v.getChurn());
    }

    /**
     * Adds the additional file statistics instance.
     *
     * @param additionalStatistics
     *         the additional statistics to add
     */
    public void add(final FileStatistics additionalStatistics) {
        statisticsPerFile.put(additionalStatistics.getFileName(), additionalStatistics);
    }

    public int getTotalChurn() {
        return totalChurn;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
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
        return statisticsPerFile.equals(that.statisticsPerFile) && latestCommitId.equals(that.latestCommitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statisticsPerFile, latestCommitId);
    }
}
