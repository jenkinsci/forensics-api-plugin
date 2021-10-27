package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.forensics.miner.ForensicsTableModel.ForensicsRow;

import static io.jenkins.plugins.forensics.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ForensicsTableModelTest {

    @Test
    void shouldCreateForensicsTableModel() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);

        assertThat(tableModel).isNotNull();
        assertThat(tableModel).hasId(ForensicsJobAction.FORENSICS_ID);
        assertThat(tableModel.getColumns())
                .hasSize(7)
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly(
                        Messages.Table_Column_File(),
                        Messages.Table_Column_AuthorsSize(),
                        Messages.Table_Column_CommitsSize(),
                        Messages.Table_Column_LastCommit(),
                        Messages.Table_Column_AddedAt(),
                        Messages.Table_Column_LOC(),
                        Messages.Table_Column_Churn()
                );

    }

    @Test
    void shouldReturnRows() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        statistics.add(createFileStatistics());
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);
        tableModel.getRows();
        assertThat(tableModel.getRows()).hasSize(1);

        Object actual = tableModel.getRows().get(0);
        assertThat(actual).isInstanceOf(ForensicsRow.class);
        assertThat(((ForensicsRow) actual)).hasAuthorsSize(0);
    }

    private FileStatistics createFileStatistics() {
        FileStatistics fileStatistics = mock(FileStatistics.class);
        CommitDiffItem commitDiffItem = mock(CommitDiffItem.class);
        when(commitDiffItem.getTotalAddedLines()).thenReturn(1);
        fileStatistics.inspectCommit(commitDiffItem);
        return fileStatistics;
    }

    @Test
    void checkForensicsRowGetters() {
        FileStatistics fileStatisticsStub = mock(FileStatistics.class);
        ForensicsRow forensicsRow = new ForensicsRow(fileStatisticsStub);

        when(fileStatisticsStub.getFileName()).thenReturn("filename");
        when(fileStatisticsStub.getNumberOfAuthors()).thenReturn(1);
        when(fileStatisticsStub.getNumberOfCommits()).thenReturn(2);
        when(fileStatisticsStub.getLastModificationTime()).thenReturn(3);
        when(fileStatisticsStub.getCreationTime()).thenReturn(4);
        when(fileStatisticsStub.getLinesOfCode()).thenReturn(5);
        when(fileStatisticsStub.getAbsoluteChurn()).thenReturn(6);

        assertThat(forensicsRow)
                .hasFileName(
                        "<a href=\"fileName.-734768633\" data-bs-toggle=\"tooltip\" data-bs-placement=\"left\" title=\"filename\">filename</a>")
                .hasAuthorsSize(1)
                .hasCommitsSize(2)
                .hasModifiedAt(3)
                .hasAddedAt(4)
                .hasLinesOfCode(5)
                .hasChurn(6);
    }

}