package io.jenkins.plugins.forensics.miner;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.util.FilteredLog;
import io.jenkins.plugins.forensics.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.util.PathStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MinerFactory}.
 *
 * @author Ullrich Hafner
 */
class MinerFactoryTest {
    @Test
    void shouldFindNothingIfThereIsNoWorkTree() {
        RepositoryMiner empty = MinerFactory.findMiner(mock(Run.class), Collections.emptyList(), TaskListener.NULL,
                new FilteredLog("Errors"));
        assertThat(empty).isInstanceOf(NullMiner.class);
    }

    @Test
    void shouldFindBlamerForFirstWorkTree() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        MinerFactory.setJenkinsFacade(jenkins);

        MinerFactory factory = mock(MinerFactory.class);
        RepositoryMiner miner = mock(RepositoryMiner.class);
        when(factory.createMiner(any(), any(), any(), any(), any())).thenReturn(Optional.of(miner));

        when(jenkins.getExtensionsFor(MinerFactory.class)).thenReturn(Collections.singletonList(factory));

        RepositoryMiner one = MinerFactory.findMiner(mock(Run.class),
                asSourceDirectories(createWorkspace("workspace")), TaskListener.NULL,
                new FilteredLog("Errors"));
        assertThat(one).isSameAs(miner);
    }
}
