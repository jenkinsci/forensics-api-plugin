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
    private static final String FILE_NAME = "file";

    /**
     * Returns the expected {@link Blamer} from the registered {@link BlamerFactory} instances.
     */
    @Test
    public void shouldReturnSelectedBlamer() {
        SCM scm = mock(SCM.class);
        Blamer nullBlamer = BlamerFactory.findBlamerFor(scm);
        assertThat(nullBlamer).isInstanceOf(NullBlamer.class);
        assertThat(nullBlamer.blame(new FileLocations())).isEmpty();

        when(scm.getKey()).thenReturn("git");
        Blamer testBlamer = BlamerFactory.findBlamerFor(scm);
        assertThat(testBlamer).isInstanceOf(TestBlamer.class);
        assertThat(testBlamer.blame(new FileLocations())).isNotEmpty();
        assertThat(testBlamer.blame(new FileLocations())).hasFiles(FILE_NAME);
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
            Blames blames = new Blames();
            blames.add(new FileBlame(FILE_NAME));
            return blames;
        }
    }
}
