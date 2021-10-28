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
    private static final String LINK = "www.link.com";
    private static final String ID = "id";
    private static final String OWNER = "owner";
    private static final String FORENSIC_ACTION = "Forensics_Action";

    @Test
    void shouldGetOwner() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);
        when(ownerStub.getDisplayName()).thenReturn(OWNER);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getOwner().getClass()).isEqualTo(ownerStub.getClass());
        assertThat(sut.getOwner().getDisplayName()).isEqualTo(sut.getOwner().getDisplayName());
    }

    @Test
    void shouldGetDisplayName() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);

        try (MockedStatic<Messages> messagesMock = mockStatic(Messages.class)) {
            messagesMock.when(Messages::Forensics_Action).thenReturn(FORENSIC_ACTION);

            ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

            assertThat(sut.getDisplayName().getClass()).isEqualTo(FORENSIC_ACTION.getClass());
            assertThat(sut.getDisplayName()).isEqualTo(FORENSIC_ACTION);
        }
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
        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(SCM_KEY).isEqualTo(sut.getScmKey());
    }

    @Test
    void shouldGetTableModel() {
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        Run<?, ?> ownerStub = mock(Run.class);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        assertThat(sut.getTableModel(ID).getClass()).isEqualTo(ForensicsTableModel.class);
    }

    @Test
    void shouldGetDynamic() {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);
        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenReturn(null);

            assertThat(sut.getDynamic(LINK, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
        }
    }

    @Test
    void shouldGetDynamicThrowNoSuchElementException() throws IOException {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);

        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        doNothing().when(staplerResponse).sendRedirect2(any(String.class));
        StaplerRequest staplerRequest = mock(StaplerRequest.class);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenThrow(NoSuchElementException.class);

            assertThat(sut.getDynamic(LINK, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
            verify(staplerResponse, times(1)).sendRedirect2(any(String.class));
        }
    }

    @Test
    void shouldGetDynamicThrowIOExceptionException() throws IOException {
        Run<?, ?> ownerStub = mock(Run.class);
        RepositoryStatistics repositoryStatisticsStub = mock(RepositoryStatistics.class);

        StaplerResponse staplerResponse = mock(StaplerResponse.class);
        doNothing().when(staplerResponse).sendRedirect2(any(String.class));
        doThrow(IOException.class).when(staplerResponse).sendRedirect2(any(String.class));

        StaplerRequest staplerRequest = mock(StaplerRequest.class);

        ForensicsViewModel sut = new ForensicsViewModel(ownerStub, repositoryStatisticsStub, SCM_KEY);

        try (MockedStatic<CommitDecoratorFactory> commitDecoratorFactory = mockStatic(CommitDecoratorFactory.class)) {
            commitDecoratorFactory.when(() -> CommitDecoratorFactory.findCommitDecorator(any(Run.class)))
                    .thenThrow(NoSuchElementException.class);

            assertThat(sut.getDynamic(LINK, staplerRequest, staplerResponse)
                    .getClass()).isEqualTo(ForensicsViewModel.class);
            verify(staplerResponse, times(1)).sendRedirect2(any(String.class));
        }
    }
}