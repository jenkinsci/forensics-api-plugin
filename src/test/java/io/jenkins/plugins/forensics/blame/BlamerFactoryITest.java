package io.jenkins.plugins.forensics.blame;

import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import hudson.scm.SCM;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link BlamerFactory}.
 *
 * @author Ullrich Hafner
 */
public class BlamerFactoryITest {
    /** Jenkins rule per suite. */
    @ClassRule
    public static final JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    /**
     * Returns the expected {@link Blamer} from the registerd {@link BlamerFactory} instances.
     */
    @Test
    public void shouldReturnSelectedBlamer() {
        SCM scm = mock(SCM.class);
        assertThat(BlamerFactory.findBlamerFor(scm)).isInstanceOf(NullBlamer.class);

        when(scm.getKey()).thenReturn("git");
        assertThat(BlamerFactory.findBlamerFor(scm)).isInstanceOf(TestBlamer.class);
    }

    /**
     * Factory that does not return a blamer.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class EmptyFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm) {
            return Optional.empty();
        }
    }

    /**
     * Factory that returns a blamer.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class ActualFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm) {
            if (StringUtils.isBlank(scm.getKey())) {
                return Optional.empty();
            }
            return Optional.of(new TestBlamer());
        }
    }

    /** A blamer for the test. */
    private static class TestBlamer extends Blamer {
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        Blames blame(final FileLocations fileLocations) {
            return new Blames();
        }
    }

}
