package io.jenkins.plugins.forensics.miner;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableColumn.ColumnType;
import io.jenkins.plugins.datatables.TableModel;

import static j2html.TagCreator.*;

/**
 * Provides the dynamic model for the details table that shows the temporal couplings of all repository files.
 *
 * <p>
 * This temporal coupling model consists of the following columns:
 * </p>
 * <ul>
 * <li>name of the first file of the coupling</li>
 * <li>name of the second file of the coupling</li>
 * <li>number of commits that changed both files</li>
 * <li>strength of the coupling in percent</li>
 * </ul>
 *
 * @author Akash Manna
 */
public class TemporalCouplingTableModel extends TableModel {
    static final String TEMPORAL_COUPLING_ID = "temporal-coupling";

    private final List<TemporalCoupling> temporalCouplings;

    TemporalCouplingTableModel(final List<TemporalCoupling> temporalCouplings) {
        super();

        this.temporalCouplings = List.copyOf(temporalCouplings);
    }

    @Override
    public String getId() {
        return TEMPORAL_COUPLING_ID;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();

        var builder = new ColumnBuilder();

        columns.add(builder.withHeaderLabel(Messages.Table_Column_File())
                .withDetailedCell()
                .withDataPropertyKey("leftFile")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_CoupledFile())
                .withDetailedCell()
                .withDataPropertyKey("rightFile")
                .withHeaderClass(ColumnCss.NONE)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_CoChanges())
                .withPlainValueCell()
                .withDataPropertyKey("coChanges")
                .withType(ColumnType.NUMBER)
                .build());
        columns.add(builder.withHeaderLabel(Messages.Table_Column_CouplingPercentage())
                .withDataPropertyKey("couplingPercentage")
                .withType(ColumnType.NUMBER)
                .build());

        return columns;
    }

    @Override
    public List<Object> getRows() {
        return temporalCouplings.stream().map(TemporalCouplingRow::new).collect(Collectors.toList());
    }

    /**
     * A table row that shows the temporal coupling of a pair of files.
     */
    public static class TemporalCouplingRow {
        private final TemporalCoupling temporalCoupling;

        TemporalCouplingRow(final TemporalCoupling temporalCoupling) {
            this.temporalCoupling = temporalCoupling;
        }

        /**
         * Shows the first file of this coupling: the column shows the name without the path. The full path is shown as
         * an additional tooltip.
         *
         * @return the file name column (as HTML span tag)
         */
        public DetailedCell<?> getLeftFile() {
            return createFileCell(temporalCoupling.getLeftFile());
        }

        /**
         * Shows the second file of this coupling: the column shows the name without the path. The full path is shown
         * as an additional tooltip.
         *
         * @return the file name column (as HTML span tag)
         */
        public DetailedCell<?> getRightFile() {
            return createFileCell(temporalCoupling.getRightFile());
        }

        private DetailedCell<?> createFileCell(final String fullPath) {
            var fileName = FilenameUtils.getName(fullPath);
            var cell = span().withText(fileName)
                    .attr("data-bs-toggle", "tooltip")
                    .attr("data-bs-placement", "left")
                    .attr("title", fullPath).render();
            return new DetailedCell<>(cell, fileName);
        }

        public int getCoChanges() {
            return temporalCoupling.getCoChanges();
        }

        public double getCouplingPercentage() {
            return temporalCoupling.getCouplingPercentage();
        }
    }
}
