package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import edu.hm.hafner.echarts.Palette;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.ModelObject;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.echarts.AsyncTrendChart;

/**
 * Creates a view for the selected link in the details table.
 *
 * @author Giulia Del Bravo
 */
public class FileDetailsView extends DefaultAsyncTableContentProvider implements ModelObject, AsyncTrendChart {

    private final String fileHash;
    private static final String FILE_NAME_PROPERTY = "fileName.";
    private final RepositoryStatistics repositoryStatistics;
    private final List<FileStatistics> fileStatistics;

    public FileDetailsView(final String fileLink, final RepositoryStatistics repositoryStatistics) {
        this.fileHash = fileLink.substring(FILE_NAME_PROPERTY.length());
        this.repositoryStatistics = repositoryStatistics;
        fileStatistics = filterStatistics();
    }

    private List<FileStatistics> filterStatistics() {
        return repositoryStatistics.getFileStatistics()
                .stream()
                .filter(f -> String.valueOf(f.getFileName().hashCode()).equals(fileHash)).collect(Collectors.toList());
    }

    public LinesChartModel createChartModel() {
        return new FileChurnTrendChart().create(fileStatistics.get(0));
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
        return Messages.FileView_Title();
    }

    @Override
    public TableModel getTableModel(final String s) {
        return new FileTableModel();
    }

    private class FileTableModel extends TableModel {

        @Override
        public String getId() {
            return null;
        }

        @Override
        public List<TableColumn> getColumns() {
            List<TableColumn> columns = new ArrayList<>();

            columns.add(new TableColumn(Messages.Table_Column_CommitId(), "commitId"));
            columns.add(new TableColumn(Messages.Table_Column_AddedLines(), "addedLines"));
            columns.add(new TableColumn(Messages.Table_Column_DeletedLines(), "deletedLines"));

            return columns;
        }

        @Override
        public List<Object> getRows() {
            return fileStatistics.get(0)
                    .getNumberOfAddedLines()
                    .keySet()
                    .stream()
                    .map(commitId -> new ForensicsRow(fileStatistics.get(0), commitId))
                    .collect(Collectors.toList());
        }
    }

    /**
     * A table row that shows details about a specific file.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class ForensicsRow {
        private final FileStatistics fileStatistics;
        private final String commitId;

        ForensicsRow(final FileStatistics fileStatistics, final String commitId) {

            this.fileStatistics = fileStatistics;
            this.commitId = commitId;
        }

        public int getAddedLines() {
            return fileStatistics.getNumberOfAddedLines().get(commitId);
        }

        public int getDeletedLines() {
            return fileStatistics.getNumberOfDeletedLines().get(commitId);
        }

        public String getCommitId() {
            return commitId;
        }

    }

    static class FileChurnTrendChart {
        private static final String ADDED_KEY = "added";
        private static final String DELETED_KEY = "deleted";

        public LinesChartModel create(final FileStatistics fileStatistics) {
            LinesDataSet dataSet = createDataSetPerCommit(fileStatistics);

            LinesChartModel model = new LinesChartModel();
            Palette[] colors = {Palette.GREEN, Palette.RED};
            model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
            model.setBuildNumbers(dataSet.getBuildNumbers());
            int index = 0;
            for (String name : dataSet.getDataSetIds()) {
                LineSeries series = new LineSeries(name, colors[index++].getNormal(), StackedMode.SEPARATE_LINES,
                        FilledMode.LINES);
                series.addAll(dataSet.getSeries(name));
                model.addSeries(series);
            }
            return model;
        }

        private LinesDataSet createDataSetPerCommit(final FileStatistics current) {
            LinesDataSet model = new LinesDataSet();
            for (String commitId : current.getNumberOfDeletedLines().keySet()) {
                model.add(commitId, computeSeries(current, commitId));
            }
            return model;
        }

        private Map<String, Integer> computeSeries(final FileStatistics fileStatistics, final String commitId) {
            Map<String, Integer> commitChanges = new HashMap<>();
            commitChanges.put(ADDED_KEY, fileStatistics.getNumberOfAddedLines().get(commitId));
            commitChanges.put(DELETED_KEY, fileStatistics.getNumberOfDeletedLines().get(commitId));
            return commitChanges;
        }
    }

}
