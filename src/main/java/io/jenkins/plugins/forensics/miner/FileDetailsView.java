package io.jenkins.plugins.forensics.miner;

import org.apache.commons.io.FilenameUtils;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.echarts.AsyncTrendChart;
import io.jenkins.plugins.echarts.JenkinsPalette;
import io.jenkins.plugins.forensics.util.CommitDecorator;

/**
 * Creates a view for the selected link in the details table.
 *
 * @author Giulia Del Bravo
 */
public class FileDetailsView extends DefaultAsyncTableContentProvider implements ModelObject, AsyncTrendChart {
    private static final String FILE_NAME_PROPERTY = "fileName.";

    private final Run<?, ?> owner;
    private final String fileHash;
    private final RepositoryStatistics repositoryStatistics;
    private final CommitDecorator decorator;
    private final FileStatistics fileStatistics;

    /**
     * Creates a new {@link FileDetailsView} instance.
     *
     * @param owner
     *         the owner (build) of this view
     * @param fileLink
     *         the file the view should be created for
     * @param repositoryStatistics
     *         the whole repository statistics
     * @param decorator
     *         renders commit links
     */
    public FileDetailsView(final Run<?, ?> owner, final String fileLink,
            final RepositoryStatistics repositoryStatistics,
            final CommitDecorator decorator) {
        super();

        this.owner = owner;
        this.fileHash = fileLink.substring(FILE_NAME_PROPERTY.length());
        this.repositoryStatistics = repositoryStatistics;
        this.decorator = decorator;
        fileStatistics = filterStatistics();
    }

    private FileStatistics filterStatistics() {
        return repositoryStatistics.getFileStatistics()
                .stream()
                .filter(f -> String.valueOf(f.getFileName().hashCode()).equals(fileHash)).findAny().orElseThrow(
                        () -> new NoSuchElementException("No file found with hash code " + fileHash));
    }

    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Should return a LinesChartModel for this file detailing the added and deleted lines over all commits analyzed.
     *
     * @return LinesChartModel for this file displaying deleted and added lines.
     */
    public LinesChartModel createChartModel() {
        return new FileChurnTrendChart().create(fileStatistics, decorator);
    }

    /**
     * Returns the repository URL for the specified commit.
     *
     * @param commit
     *         the commit to get the URL for
     *
     * @return the repository URL
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getCommitUrl(final String commit) {
        return decorator.getRawLink(commit);
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    @Override
    public String getBuildTrendModel() {
        return new JacksonFacade().toJson(createChartModel());
    }

    @Override
    public boolean isTrendVisible() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return Messages.FileView_Title(FilenameUtils.getName(getFullPath()));
    }

    public String getFullPath() {
        return fileStatistics.getFileName();
    }

    @Override
    public TableModel getTableModel(final String id) {
        return new FileTableModel();
    }

    private class FileTableModel extends TableModel {
        @Override
        public String getId() {
            return "forensics-details";
        }

        @Override
        public List<TableColumn> getColumns() {
            List<TableColumn> columns = new ArrayList<>();

            columns.add(new TableColumn(Messages.Table_Column_CommitId(), "commitId"));
            columns.add(new TableColumn(Messages.Table_Column_Author(), "author"));
            columns.add(new TableColumn(Messages.Table_Column_AddedLines(), "addedLines"));
            columns.add(new TableColumn(Messages.Table_Column_DeletedLines(), "deletedLines"));

            return columns;
        }

        @Override
        public List<Object> getRows() {
            return fileStatistics
                    .getCommits()
                    .stream()
                    .map(commit -> new ForensicsRow(commit, decorator))
                    .collect(Collectors.toList());
        }
    }

    /**
     * A table row that shows details about a specific file.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class ForensicsRow {
        private final String id;
        private final String author;
        private final int totalAddedLines;
        private final int totalDeletedLines;

        ForensicsRow(final CommitDiffItem commit, final CommitDecorator decorator) {
            id = decorator.asLink(commit.getId());
            author = commit.getAuthor();
            totalAddedLines = commit.getTotalAddedLines();
            totalDeletedLines = commit.getTotalDeletedLines();
        }

        public int getAddedLines() {
            return totalAddedLines;
        }

        public int getDeletedLines() {
            return totalDeletedLines;
        }

        public String getCommitId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }
    }

    static class FileChurnTrendChart {
        private static final String ADDED_KEY = "added";
        private static final String DELETED_KEY = "deleted";

        public LinesChartModel create(final FileStatistics fileStatistics,
                final CommitDecorator decorator) {
            var dataSet = createDataSetPerCommit(fileStatistics, decorator);

            var model = new LinesChartModel(dataSet);
            model.setDomainAxisItemName("Commit");
            var added = new LineSeries(Messages.TrendChart_Churn_Legend_Added(), JenkinsPalette.GREEN.normal(),
                    StackedMode.SEPARATE_LINES, FilledMode.FILLED);
            added.addAll(dataSet.getSeries(ADDED_KEY));
            var deleted = new LineSeries(Messages.TrendChart_Churn_Legend_Deleted(), JenkinsPalette.RED.normal(),
                    StackedMode.SEPARATE_LINES, FilledMode.FILLED);
            deleted.addAll(dataSet.getSeries(DELETED_KEY));

            model.addSeries(added, deleted);

            return model;
        }

        private LinesDataSet createDataSetPerCommit(final FileStatistics current,
                final CommitDecorator decorator) {
            var model = new LinesDataSet();
            for (CommitDiffItem commit : current.getCommits()) {
                model.add(decorator.asText(commit.getId()), computeSeries(commit));
            }
            return model;
        }

        private Map<String, Integer> computeSeries(final CommitDiffItem commit) {
            Map<String, Integer> commitChanges = new HashMap<>();
            commitChanges.put(ADDED_KEY, commit.getTotalAddedLines());
            commitChanges.put(DELETED_KEY, commit.getTotalDeletedLines());
            return commitChanges;
        }
    }
}
