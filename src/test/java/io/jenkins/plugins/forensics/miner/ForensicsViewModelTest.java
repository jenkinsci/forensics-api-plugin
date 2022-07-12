package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.PieChartModel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
 * @author Ullrich Hafner
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

        runWithNullDecorator(model,
                m -> assertThat(m.getDynamic(createLink(), mock(StaplerRequest.class), mock(StaplerResponse.class)))
                        .isInstanceOf(FileDetailsView.class)
        );
    }

    @Test
    void shouldThrowNoSuchElementExceptionInGetDynamic() throws IOException {
        ForensicsViewModel model = new ForensicsViewModel(mock(Run.class), new RepositoryStatistics(), SCM_KEY);

        runWithNullDecorator(model,
                m -> {
                    try {
                        StaplerResponse staplerResponse = mock(StaplerResponse.class);
                        assertThat(m.getDynamic("wrong-link", mock(StaplerRequest.class), staplerResponse)).isSameAs(m);
                        verify(staplerResponse, times(1)).sendRedirect2(any(String.class));
                    }
                    catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                });
    }

    @Test
    void shouldThrowIOExceptionInGetDynamic() throws IOException {
        ForensicsViewModel model = new ForensicsViewModel(mock(Run.class), new RepositoryStatistics(), SCM_KEY);

        runWithNullDecorator(model,
                m -> {
                    try {
                        StaplerResponse staplerResponse = mock(StaplerResponse.class);
                        doThrow(IOException.class).when(staplerResponse).sendRedirect2(any(String.class));
                        assertThat(m.getDynamic("wrong-link", mock(StaplerRequest.class), staplerResponse)).isSameAs(m);
                    }
                    catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                });
    }

    @SuppressFBWarnings(value = "RCN", justification = "False positive")
    private void runWithNullDecorator(final ForensicsViewModel model, final Consumer<ForensicsViewModel> modelConsumer) {
        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory
                    .when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenReturn(new NullDecorator());

            modelConsumer.accept(model);
        }
    }

    private String createLink() {
        return "filename." + FILE_NAME.hashCode();
    }
}

