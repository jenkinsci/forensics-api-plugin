package io.jenkins.plugins.forensics.miner;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

/**
 * Provides access to the SCM commit statistics of all repository files up to a specific commit.
 *
 * @author Ullrich Hafner
 */
public class RepositoryStatistics implements Serializable {
    @Serial
    private static final long serialVersionUID = 8L; // release 0.8.0

    @CheckForNull
    @SuppressWarnings("PMD.LooseCoupling")
    private HashMap<String, FileStatistics> statisticsPerFile; // before 0.8.0, mapped in readResolve

    @SuppressWarnings("PMD.LooseCoupling")
    private transient HashMap<String, FileStatistics> statisticsMapping = new HashMap<>();
    @SuppressWarnings("PMD.LooseCoupling")
    private ArrayList<FileStatistics> fileStatistics = new ArrayList<>();

    private String latestCommitId; // @since 0.8.0
    private CommitStatistics statistics = new CommitStatistics();
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
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    @SuppressWarnings("PMD.NullAssignment")
    protected Object readResolve() {
        if (latestCommitId == null) {
            latestCommitId = StringUtils.EMPTY;
        }
        if (statisticsPerFile == null) { // since 0.8.0: rebuild mapping
            statisticsMapping = new HashMap<>();
            fileStatistics.forEach(s -> statisticsMapping.put(s.getFileName(), s));
        }
        else { // before 0.8.0: restore map
            statisticsMapping = statisticsPerFile;
            statisticsPerFile = null; // set to null to remove the field from serialization
        }

        return this;
    }

    /**
     * Called before serialization to fill the fileStatistics field.
     *
     * @return this
     */
    protected Object writeReplace() {
        fileStatistics = new ArrayList<>(statisticsMapping.values());

        return this;
    }

    /**
     * Returns whether the repository is empty.
     *
     * @return {@code true} if the repository is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return statisticsMapping.isEmpty();
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
        return statisticsMapping.size();
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
        return statisticsMapping.containsKey(fileName);
    }

    /**
     * Returns the absolute file names of the files that are part of the repository.
     *
     * @return the file names
     */
    public Set<String> getFiles() {
        return Collections.unmodifiableSet(statisticsMapping.keySet());
    }

    /**
     * Returns the statistics for all repository files.
     *
     * @return the statistics
     */
    public Collection<FileStatistics> getFileStatistics() {
        return Collections.unmodifiableCollection(statisticsMapping.values());
    }

    /**
     * Returns the mapping of file names to statistics.
     *
     * @return the mapping of file names to statistics
     */
    public Map<String, FileStatistics> getMapping() {
        return Collections.unmodifiableMap(statisticsMapping);
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
            return statisticsMapping.get(fileName);
        }
        throw new NoSuchElementException("No information for file %s stored".formatted(fileName));
    }

    /**
     * Adds and inspects the specified commits.
     *
     * @param commits
     *         the additional commits
     */
    public void addAll(final List<CommitDiffItem> commits) {
        var builder = new FileStatisticsBuilder();
        for (CommitDiffItem commit : commits) {
            if (commit.isDelete()) {
                statisticsMapping.remove(commit.getOldPath());
            }
            else if (commit.isMove()) {
                var existing = statisticsMapping.remove(commit.getOldPath());
                if (existing == null) {
                    statisticsMapping.putIfAbsent(commit.getNewPath(), builder.build(commit.getNewPath()));
                }
                else {
                    statisticsMapping.put(commit.getNewPath(), existing);
                }
                statisticsMapping.get(commit.getNewPath()).inspectCommit(commit);
            }
            else {
                statisticsMapping.putIfAbsent(commit.getNewPath(), builder.build(commit.getNewPath()));
                statisticsMapping.get(commit.getNewPath()).inspectCommit(commit);
            }
        }
        statistics = new CommitStatistics(commits);
        updateTotalLoc();
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
        statisticsMapping.merge(additionalStatistics.getFileName(), additionalStatistics, this::merge);
        updateTotalLoc();
    }

    private void updateTotalLoc() {
        totalLinesOfCode = sum(FileStatistics::getLinesOfCode);
        totalChurn = sum(FileStatistics::getAbsoluteChurn);
    }

    private int sum(final ToIntFunction<FileStatistics> property) {
        return statisticsMapping.values().stream().mapToInt(property).sum();
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

    public CommitStatistics getLatestStatistics() {
        return statistics;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (RepositoryStatistics) o;
        return statisticsMapping.equals(that.statisticsMapping) && latestCommitId.equals(that.latestCommitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statisticsMapping, latestCommitId);
    }
}
