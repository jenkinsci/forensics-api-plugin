package io.jenkins.plugins.forensics.miner;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.PieChartModel;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Run;

import io.jenkins.plugins.forensics.util.CommitDecoratorFactory;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ForensicsViewModel}.
 *
 * @author Johannes Walter
 */
class ForensicsViewModelTest {
    private static final String SCM_KEY = "scmKey";

    @Test
    void shouldGetOwner() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        final String expected = "owner";
        when(ownerStub.getDisplayName()).thenReturn(expected);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getOwner().getClass()).isEqualTo(ownerStub.getClass());
        assertThat(sut.getOwner().getDisplayName()).isEqualTo(sut.getOwner().getDisplayName());
    }

    @Test
    void shouldGetDisplayName() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        final String expected = "Forensics_Action";
        MockedStatic<Messages> messagesMock = mockStatic(Messages.class);
        messagesMock.when(Messages::Forensics_Action).thenReturn(expected);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getDisplayName().getClass()).isEqualTo(expected.getClass());
        assertThat(sut.getDisplayName()).isEqualTo(expected);
        messagesMock.close();
    }

    @Test
    void shouldGetAuthorsModel() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        SizePieChart sizePieChartMock = mock(SizePieChart.class);
        final String expected = new JacksonFacade().toJson(new PieChartModel());
        when(sizePieChartMock.create(any(RepositoryStatistics.class),
                any(), anyInt())).thenReturn(new PieChartModel());

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getAuthorsModel()).isEqualTo(expected);
    }

    @Test
    void shouldGetCommitsModel() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        SizePieChart sizePieChartMock = mock(SizePieChart.class);
        final String expected = new JacksonFacade().toJson(new PieChartModel());
        when(sizePieChartMock.create(any(RepositoryStatistics.class),
                any(), anyInt())).thenReturn(new PieChartModel());

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getCommitsModel()).isEqualTo(expected);
    }

    @Test
    void shouldGetScmKey() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        final String expected = "scmKey";
        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, expected);

        assertEquals(expected, sut.getScmKey());
    }

    @Test
    void shouldGetTableModel() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        final String id = "id";

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getTableModel(id).getClass()).isEqualTo(ForensicsTableModel.class);
    }

    @Test
    void shouldGetDynamic() {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);
        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        final String link = "www.link.com";

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenReturn(null);

            assertThat(sut.getDynamic(link, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
        }
    }

    @Test
    void shouldGetDynamicThrowNoSuchElementException() throws IOException {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        doNothing().when(staplerResponse).sendRedirect2(any(String.class));
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        final String link = "www.link.com";

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenThrow(NoSuchElementException.class);

            assertThat(sut.getDynamic(link, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
            verify(staplerResponse, times(1)).sendRedirect2(any(String.class));
        }
    }

    @Test
    void shouldGetDynamicThrowIOExceptionException() throws IOException {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        doNothing().when(staplerResponse).sendRedirect2(any(String.class));
        doThrow(IOException.class).when(staplerResponse).sendRedirect2(any(String.class));

        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        final String link = "www.link.com";

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenThrow(NoSuchElementException.class);

            assertThat(sut.getDynamic(link, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
            verify(staplerResponse, times(1)).sendRedirect2(any(String.class));
        }
    }
}