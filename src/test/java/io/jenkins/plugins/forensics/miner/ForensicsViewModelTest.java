package io.jenkins.plugins.forensics.miner;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.PieChartModel;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Run;

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;
import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;
import io.jenkins.plugins.forensics.util.CommitDecoratorFactory;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ForensicsViewModel}.
 *
 * @author Johannes Walter
 */
class ForensicsViewModelTest {
    private static final String SCM_KEY = "scmKey";
    private static final String FILE_NAME = "file.name";

    @Test
    void shouldHandleEmptyModel() {
        Run<?, ?> owner = mock(Run.class);

        ForensicsViewModel model = new ForensicsViewModel(owner, new RepositoryStatistics(), SCM_KEY);

        assertThat(model)
                .hasOwner(owner)
                .hasDisplayName(Messages.Forensics_Action())
                .hasAuthorsModel(createEmptyAuthorsModel())
                .hasCommitsModel(createEmptyCommitsModel())
                .hasScmKey(SCM_KEY);

        assertThat(model.getTableModel("id-does-not-matter"))
                .hasId(ForensicsJobAction.FORENSICS_ID)
                .hasNoRows();
    }

    private String createEmptyAuthorsModel() {
        return new JacksonFacade().toJson(new PieChartModel());
    }

    private String createEmptyCommitsModel() {
        return createEmptyAuthorsModel();
    }

    @Test
    void shouldGetFileDetailsViewInDynamic() {
        RepositoryStatistics repositoryStatistics = new RepositoryStatistics();
        FileStatistics fileStatistics = new FileStatisticsBuilder().build(FILE_NAME);
        repositoryStatistics.add(fileStatistics);

        ForensicsViewModel model = new ForensicsViewModel(mock(Run.class), repositoryStatistics, SCM_KEY);

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            findNullDecorator(commitDecoratorFactory);

            assertThat(model.getDynamic(createLink(), mock(StaplerRequest.class), mock(StaplerResponse.class)))
                    .isInstanceOf(FileDetailsView.class);
        }
    }

    private void findNullDecorator(
            final MockedStatic<CommitDecoratorFactory> commitDecoratorFactory) {
        commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                .thenReturn(new NullDecorator());
    }

    private String createLink() {
        return "filename." + FILE_NAME.hashCode();
    }

    @Test
    void shouldThrowNoSuchElementExceptionInGetDynamic() throws IOException {
        StaplerResponse staplerResponse = mock(StaplerResponse.class);

        ForensicsViewModel model = new ForensicsViewModel(mock(Run.class), new RepositoryStatistics(), SCM_KEY);

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            findNullDecorator(commitDecoratorFactory);

            assertThat(model.getDynamic("wrong-link", mock(StaplerRequest.class), staplerResponse)).isSameAs(model);
            verify(staplerResponse, times(1)).sendRedirect2(any(String.class));

            doThrow(IOException.class).when(staplerResponse).sendRedirect2(any(String.class));
            assertThat(model.getDynamic("wrong-link", mock(StaplerRequest.class), staplerResponse)).isSameAs(model);
        }
    }
}
