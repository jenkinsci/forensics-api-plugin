package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import java.util.List;

import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.forensics.miner.TemporalCouplingTableModel.TemporalCouplingRow;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Tests the class {@link TemporalCouplingTableModel}.
 *
 * @author Akash Manna
 */
class TemporalCouplingTableModelTest {
    private static final String LEFT_FILE_NAME = "Left.java";
    private static final String RIGHT_FILE_NAME = "Right.java";
    private static final String LEFT_FILE = "src/main/java/" + LEFT_FILE_NAME;
    private static final String RIGHT_FILE = "src/main/java/" + RIGHT_FILE_NAME;
    private static final int CO_CHANGES = 12;
    private static final double COUPLING_RATIO = 0.75;

    @Test
    void shouldCreateTemporalCouplingTableModel() {
        var tableModel = new TemporalCouplingTableModel(List.of());

        assertThat(tableModel).isNotNull();
        assertThat(tableModel).hasId(TemporalCouplingTableModel.TEMPORAL_COUPLING_ID);
        assertThat(tableModel.getColumns())
                .hasSize(4)
                .extracting(TableColumn::getHeaderLabel)
                .containsExactly(
                        Messages.Table_Column_File(),
                        Messages.Table_Column_CoupledFile(),
                        Messages.Table_Column_CoChanges(),
                        Messages.Table_Column_CouplingPercentage()
                );
        assertThatJson(tableModel.getColumns().get(0).getDefinition()).node("render")
                .isEqualTo("""
                        {
                          "_" : "display",
                          "sort": "sort"
                        }
                        """);
        assertThatJson(tableModel.getColumns().get(1).getDefinition()).node("render")
                .isEqualTo("""
                        {
                          "_" : "display",
                          "sort": "sort"
                        }
                        """);
        assertThatJson(tableModel.getColumns().get(2).getDefinition()).node("render").isAbsent();
        assertThatJson(tableModel.getColumns().get(3).getDefinition()).node("render").isAbsent();
    }

    @Test
    void shouldHaveNoRowsForEmptyCouplings() {
        var tableModel = new TemporalCouplingTableModel(List.of());

        assertThat(tableModel).hasNoRows();
    }

    @Test
    void shouldReturnRows() {
        var tableModel = new TemporalCouplingTableModel(List.of(createCoupling()));

        assertThat(tableModel.getRows()).hasSize(1);

        var actual = tableModel.getRows().get(0);
        assertThat(actual).isInstanceOf(TemporalCouplingRow.class);
        assertThat((TemporalCouplingRow) actual)
                .hasCoChanges(CO_CHANGES)
                .hasCouplingPercentage(75.0);
    }

    @Test
    void shouldShowFileNamesWithFullPathAsTooltip() {
        var row = new TemporalCouplingRow(createCoupling());
        var expectedLeftCell = createCellHtml(LEFT_FILE_NAME, LEFT_FILE);
        var expectedRightCell = createCellHtml(RIGHT_FILE_NAME, RIGHT_FILE);

        assertThat(row.getLeftFile()).isInstanceOfSatisfying(DetailedCell.class,
                cell -> {
                    assertThat(cell.getDisplay()).isEqualTo(expectedLeftCell);
                    assertThat(cell.getSort()).isEqualTo(LEFT_FILE_NAME);
                });
        assertThat(row.getRightFile()).isInstanceOfSatisfying(DetailedCell.class,
                cell -> {
                    assertThat(cell.getDisplay()).isEqualTo(expectedRightCell);
                    assertThat(cell.getSort()).isEqualTo(RIGHT_FILE_NAME);
                });
    }

    private String createCellHtml(final String fileName, final String fullPath) {
        return "<span data-bs-toggle=\"tooltip\" data-bs-placement=\"left\" title=\"%s\">%s</span>"
                .formatted(fullPath, fileName);
    }

    private TemporalCoupling createCoupling() {
        return new TemporalCoupling(LEFT_FILE, RIGHT_FILE, CO_CHANGES, COUPLING_RATIO);
    }
}
