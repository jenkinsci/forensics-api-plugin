package io.jenkins.plugins.forensics.miner;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableModel;

import static j2html.TagCreator.*;

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
        return ForensicsJobAction.FORENSICS_ID;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        var builder = new ColumnBuilder();

        columns.add(builder.withHeaderLabel(Messages.Table_Column_File())
                .withDataPropertyKey("fileName")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_AuthorsSize())
                .withDataPropertyKey("authorsSize")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_CommitsSize())
                .withDataPropertyKey("commitsSize")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_LastCommit())
                .withDataPropertyKey("modifiedAt")
                .withHeaderClass(ColumnCss.DATE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_AddedAt())
                .withDataPropertyKey("addedAt")
                .withHeaderClass(ColumnCss.DATE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_LOC())
                .withDataPropertyKey("linesOfCode")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_Churn())
                .withDataPropertyKey("churn")
                .withHeaderClass(ColumnCss.NONE)
                .build());

        return columns;
    }

    @Override
    public List<Object> getRows() {
        return statistics.getFileStatistics().stream().map(ForensicsRow::new).collect(Collectors.toList());
    }

    /**
     * A table row that shows the source control statistics.
     */
    public static class ForensicsRow {
        private final FileStatistics fileStatistics;

        ForensicsRow(final FileStatistics fileStatistics) {
            this.fileStatistics = fileStatistics;
        }

        /**
         * SHows the file name column: the column shows the name without the path. The full path is shown
         * as additional tooltip.
         *
         * @return the file name column (as HTML a tag)
         */
        public String getFileName() {
            var fullPath = fileStatistics.getFileName();

            return a().withHref("fileName." + fullPath.hashCode())
                    .withText(FilenameUtils.getName(fullPath))
                    .attr("data-bs-toggle", "tooltip")
                    .attr("data-bs-placement", "left")
                    .withTitle(fullPath).render();
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

        public int getLinesOfCode() {
            return fileStatistics.getLinesOfCode();
        }

        public int getChurn() {
            return fileStatistics.getAbsoluteChurn();
        }
    }
}
