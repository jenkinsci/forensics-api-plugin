package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

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
     * Returns whether the latest commit ID has been set.
     *
     * @return {@code true} if the latest commit ID has been set, {@code false} otherwise
     */
    public boolean hasLatestCommitId() {
        return StringUtils.isNotBlank(latestCommitId);
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

    public void addAll(final List<Commit> commits) {
        FileStatisticsBuilder builder = new FileStatisticsBuilder();
        for (Commit commit : commits) {
            if (commit.isDelete()) {
                statisticsPerFile.remove(commit.getOldPath());
            }
            else if (commit.isMove()) {
                FileStatistics existing = statisticsPerFile.remove(commit.getOldPath());
                statisticsPerFile.put(commit.getNewPath(), existing);
                existing.inspectCommit(commit);
            }
            else {
                statisticsPerFile.putIfAbsent(commit.getNewPath(), builder.build(commit.getNewPath()));
                statisticsPerFile.get(commit.getNewPath()).inspectCommit(commit);
            }
        }
    }

    /**
     * Adds all additional file statistics.
     *
     * @param additionalStatistics
     *         the additional statistics to add
     */
    public void addAll(final Collection<FileStatistics> additionalStatistics) {
        additionalStatistics.forEach(this::add);
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
        statisticsPerFile.merge(additionalStatistics.getFileName(), additionalStatistics, this::merge);
        totalLinesOfCode = sum(FileStatistics::getLinesOfCode);
        totalChurn = sum(FileStatistics::getAbsoluteChurn);
    }

    private int sum(final ToIntFunction<FileStatistics> property) {
        return statisticsPerFile.values().stream().mapToInt(property).sum();
    }

    private FileStatistics merge(final FileStatistics existing, final FileStatistics additional) {
        existing.inspectCommits(additional.getCommits());
        return existing;
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
