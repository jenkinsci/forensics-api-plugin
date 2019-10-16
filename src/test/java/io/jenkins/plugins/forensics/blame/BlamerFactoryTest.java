package io.jenkins.plugins.forensics.blame;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.util.FilteredLog;
import io.jenkins.plugins.forensics.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.util.PathStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link BlamerFactory}.
 *
 * @author Ullrich Hafner
 */
class BlamerFactoryTest {
    @Test
    void shouldFindNothingIfThereIsNoWorkTree() {
        Blamer empty = BlamerFactory.findBlamer(mock(Run.class), Collections.emptyList(), TaskListener.NULL,
                new FilteredLog("Errors"));
        assertThat(empty).isInstanceOf(NullBlamer.class);
    }

    @Test
    void shouldFindBlamerForFirstWorkTree() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        BlamerFactory.setJenkinsFacade(jenkins);

        BlamerFactory factory = mock(BlamerFactory.class);
        Blamer blamer = mock(Blamer.class);
        when(factory.createBlamer(any(), any(), any(), any(), any())).thenReturn(Optional.of(blamer));

        when(jenkins.getExtensionsFor(BlamerFactory.class)).thenReturn(Collections.singletonList(factory));

        Blamer one = BlamerFactory.findBlamer(mock(Run.class),
                asSourceDirectories(createWorkspace("workspace")), TaskListener.NULL,
                new FilteredLog("Errors"));
        assertThat(one).isSameAs(blamer);
    }
}
