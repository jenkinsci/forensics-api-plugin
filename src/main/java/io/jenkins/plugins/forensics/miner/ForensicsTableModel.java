package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.api.TableColumn;
import io.jenkins.plugins.datatables.api.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.api.TableModel;

/**
 * Provides the dynamic model for the details table that shows the source control file statistics.
 *
 * <p>
 * This forensics model consists of the following columns:
 * </p>
 * <ul>
 * <li>file name</li>
 * <li>total number of different authors</li>
 * <li>total number of commits</li>
 * <li>time of last commit</li>
 * <li>time of first commit</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public class ForensicsTableModel extends TableModel {
    private final RepositoryStatistics statistics;

    ForensicsTableModel(final RepositoryStatistics statistics) {
        super();

        this.statistics = statistics;
    }

    @Override
    public String getId() {
        return JobAction.FORENSICS_ID;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        columns.add(new TableColumn(Messages.Table_Column_File(), "fileName").setWidth(2));
        columns.add(new TableColumn(Messages.Table_Column_AuthorsSize(), "authorsSize"));
        columns.add(new TableColumn(Messages.Table_Column_CommitsSize(), "commitsSize"));
        columns.add(new TableColumn(Messages.Table_Column_LastCommit(), "modifiedAt")
                .setWidth(2)
                .setHeaderClass(ColumnCss.DATE));
        columns.add(new TableColumn(Messages.Table_Column_AddedAt(), "addedAt")
                .setWidth(2)
                .setHeaderClass(ColumnCss.DATE));

        return columns;
    }

    @Override
    public List<Object> getRows() {
        return statistics.getFileStatistics().stream().map(ForensicsRow::new).collect(Collectors.toList());
    }

    /**
     * A table row that shows the source control statistics.
     */
    @SuppressWarnings("PMD.DataClass") // Used to automatically convert to JSON object
    public static class ForensicsRow  {
        private final FileStatistics fileStatistics;

        ForensicsRow(final FileStatistics fileStatistics) {
            this.fileStatistics = fileStatistics;
        }

        public String getFileName() {
            return fileStatistics.getFileName();
        }

        public int getAuthorsSize() {
            return fileStatistics.getNumberOfAuthors();
        }

        public int getCommitsSize() {
            return fileStatistics.getNumberOfCommits();
        }

        public int getModifiedAt() {
            return fileStatistics.getLastModificationTime();
        }

        public int getAddedAt() {
            return fileStatistics.getCreationTime();
        }
    }
}
