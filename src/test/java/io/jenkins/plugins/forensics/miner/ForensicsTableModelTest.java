package io.jenkins.plugins.forensics.miner;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;


import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.assertThat;


class ForensicsTableModelTest {
    private static final String FILE = "file";
    private static final TreeString FILE_TREE_STRING = new TreeStringBuilder().intern(FILE);
    private static final int ONE_DAY = 60 * 60 * 24;

    @Test
    void shouldCreateForensicsTableModel(){
        RepositoryStatistics statistics = new RepositoryStatistics();
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);
        assertThat(tableModel).isNotNull();
    }


    @Test
    void getId() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);
        assertThat(tableModel.getId()).isEqualTo(ForensicsJobAction.FORENSICS_ID);
    }

    @Test
    void getColumns() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);

        assertThat(tableModel.getColumns().size()).isEqualTo(7);
        assertThat(tableModel.getColumns().get(0).getHeaderLabel()).isEqualTo(Messages.Table_Column_File());
        assertThat(tableModel.getColumns().get(1).getHeaderLabel()).isEqualTo(Messages.Table_Column_AuthorsSize());
        assertThat(tableModel.getColumns().get(2).getHeaderLabel()).isEqualTo(Messages.Table_Column_CommitsSize());
        assertThat(tableModel.getColumns().get(3).getHeaderLabel()).isEqualTo(Messages.Table_Column_LastCommit());
        assertThat(tableModel.getColumns().get(4).getHeaderLabel()).isEqualTo(Messages.Table_Column_AddedAt());
        assertThat(tableModel.getColumns().get(5).getHeaderLabel()).isEqualTo(Messages.Table_Column_LOC());
        assertThat(tableModel.getColumns().get(6).getHeaderLabel()).isEqualTo(Messages.Table_Column_Churn());
    }

    @Test
    void getRows() {
        RepositoryStatistics statistics = new RepositoryStatistics();
        statistics.addAll(Collections.singleton(createFileStatistics()));
        ForensicsTableModel tableModel = new ForensicsTableModel(statistics);
        tableModel.getRows();
        assertThat(tableModel.getRows().size()).isEqualTo(1);
    }


    private FileStatistics createFileStatistics() {
        FileStatistics fileStatistics = new FileStatisticsBuilder().build(FILE);
        CommitDiffItem commit = new CommitDiffItem("1", "one", ONE_DAY * 9)
                .setNewPath(FILE_TREE_STRING);
        fileStatistics.inspectCommit(commit);
        return fileStatistics;
    }

}