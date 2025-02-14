package io.jenkins.plugins.forensics.reference;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.FilteredLog;

import java.util.Optional;

import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceFinder}.
 *
 * @author Ullrich Hafner
 */
class ReferenceFinderTest {
    @Test
    void shouldNotFindReferenceIfThereIsNoAction() {
        var finder = new ReferenceFinder();

        assertThat(finder.findReference(mock(Run.class), createLogger())).isEmpty();
    }

    @Test
    void shouldNotFindReferenceIfThereActionWithOutResult() {
        var finder = new ReferenceFinder();

        Run<?, ?> build = mock(Run.class);
        ReferenceBuild reference = mock(ReferenceBuild.class);
        when(build.getAction(ReferenceBuild.class)).thenReturn(reference);
        assertThat(finder.findReference(build, createLogger())).isEmpty();
    }

    @Test
    void shouldFindReference() {
        var finder = new ReferenceFinder();

        Run<?, ?> build = mock(Run.class);
        ReferenceBuild reference = mock(ReferenceBuild.class);
        when(build.getAction(ReferenceBuild.class)).thenReturn(reference);

        Run<?, ?> found = mock(Run.class);
        when(reference.getReferenceBuild()).thenReturn(Optional.of(found));

        assertThat(finder.findReference(build, createLogger())).contains(found);
    }

    private FilteredLog createLogger() {
        return new FilteredLog("Error");
    }
}
