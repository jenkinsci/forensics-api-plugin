package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import java.util.List;

import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;
import io.jenkins.plugins.forensics.miner.ForensicsTableModel.ForensicsRow;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

class ForensicsTableModelTest {
    private static final String FILE = "file";
    private static final String OTHER_FILE = "other-file";
    private static final String UNCOUPLED_FILE = "uncoupled-file";
    private static final TreeString FILE_TREE_STRING = new TreeStringBuilder().intern(FILE);
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldCreateForensicsTableModel() {
        var statistics = new RepositoryStatistics();
        var tableModel = new ForensicsTableModel(statistics);

        assertThat(tableModel).isNotNull();
        assertThat(tableModel).hasId(ForensicsJobAction.FORENSICS_ID);
        assertThat(tableModel.getColumns())
                .hasSize(8)
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly(
                        Messages.Table_Column_File(),
                        Messages.Table_Column_AuthorsSize(),
                        Messages.Table_Column_CommitsSize(),
                        Messages.Table_Column_LastCommit(),
                        Messages.Table_Column_AddedAt(),
                        Messages.Table_Column_LOC(),
                        Messages.Table_Column_Churn(),
                        Messages.Table_Column_MaxCoupling()
                );
        assertThatJson(tableModel.getColumns().get(0).getDefinition()).node("render")
                .isEqualTo("""
                        {
                          "_" : "display",
                          "sort": "sort"
                        }
                        """);
        assertThatJson(tableModel.getColumns().get(1).getDefinition()).node("render").isAbsent();
    }

    @Test
    void shouldReturnRows() {
        var statistics = new RepositoryStatistics();
        statistics.add(createFileStatistics());
        var tableModel = new ForensicsTableModel(statistics);
        tableModel.getRows();
        assertThat(tableModel.getRows()).hasSize(1);

        var actual = tableModel.getRows().get(0);
        assertThat(actual).isInstanceOf(ForensicsRow.class);
        assertThat((ForensicsRow) actual).hasAuthorsSize(1);
    }

    @Test
    void shouldShowNoCouplingIfNoCouplingsHaveBeenMined() {
        var statistics = new RepositoryStatistics();
        statistics.add(createFileStatistics());

        var tableModel = new ForensicsTableModel(statistics);

        assertThat((ForensicsRow) tableModel.getRows().get(0)).hasMaxCoupling(0);
    }

    @Test
    void shouldShowTheStrongestCouplingOfAFile() {
        var statistics = new RepositoryStatistics();
        statistics.add(createFileStatistics());
        statistics.setTemporalCouplings(List.of(
                new TemporalCoupling(FILE, OTHER_FILE, 3, 0.25),
                new TemporalCoupling(UNCOUPLED_FILE, FILE, 9, 0.8),
                new TemporalCoupling(OTHER_FILE, UNCOUPLED_FILE, 20, 1.0)));

        var tableModel = new ForensicsTableModel(statistics);

        assertThat((ForensicsRow) tableModel.getRows().get(0)).hasMaxCoupling(80.0);
    }

    private FileStatistics createFileStatistics() {
        var fileStatistics = new FileStatisticsBuilder().build(FILE);
        fileStatistics.inspectCommit(new CommitDiffItem("1", "one", ONE_DAY)
                .addLines(1)
                .setNewPath(FILE_TREE_STRING));
        return fileStatistics;
    }

    @Test
    void checkForensicsRowGetters() {
        FileStatistics fileStatisticsStub = mock(FileStatistics.class);
        var forensicsRow = new ForensicsRow(fileStatisticsStub, 7.5);

        when(fileStatisticsStub.getFileName()).thenReturn("filename");
        when(fileStatisticsStub.getNumberOfAuthors()).thenReturn(1);
        when(fileStatisticsStub.getNumberOfCommits()).thenReturn(2);
        when(fileStatisticsStub.getLastModificationTime()).thenReturn(3);
        when(fileStatisticsStub.getCreationTime()).thenReturn(4);
        when(fileStatisticsStub.getLinesOfCode()).thenReturn(5);
        when(fileStatisticsStub.getAbsoluteChurn()).thenReturn(6);

        var fileName = "<a href=\"fileName.-734768633\" data-bs-toggle=\"tooltip\" data-bs-placement=\"left\" title=\"filename\">filename</a>";
        assertThat(forensicsRow)
                .hasAuthorsSize(1)
                .hasCommitsSize(2)
                .hasModifiedAt(3)
                .hasAddedAt(4)
                .hasLinesOfCode(5)
                .hasChurn(6)
                .hasMaxCoupling(7.5);
        assertThat(forensicsRow.getFileName()).isInstanceOfSatisfying(DetailedCell.class,
                cell -> {
                    assertThat(cell.getDisplay()).isEqualTo(fileName);
                    assertThat(cell.getSort()).isEqualTo("filename");
                });
    }
}
