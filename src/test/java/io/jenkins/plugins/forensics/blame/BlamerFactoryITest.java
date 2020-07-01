package io.jenkins.plugins.forensics.blame;

import java.util.Collection;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.util.FilteredLog;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.blame.FileBlame.FileBlameBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static io.jenkins.plugins.util.PathStubs.*;
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
    private static final String NO_SUITABLE_BLAMER_FOUND = "-> No suitable blamer found.";
    private static final String ACTUAL_FACTORY_NULL_BLAMER = "ActualFactory returned NullBlamer";
    private static final String EMPTY_FACTORY_NULL_BLAMER = "EmptyFactory returned NullBlamer";
    private static final String ACTUAL_FACTORY_CREATED_A_BLAMER = "ActualFactory created a blamer";

    /** Verifies that a {@link NullBlamer} will be returned if no suitable blamer has been found. */
    @Test
    public void shouldSelectNullBlamer() {
        FilteredLog log = new FilteredLog("Foo");
        Blamer nullBlamer = createBlamer("/", log);

        assertThat(nullBlamer).isInstanceOf(NullBlamer.class);
        assertThat(nullBlamer.blame(new FileLocations(), log)).isEmpty();
        assertThat(log.getErrorMessages()).contains(NO_SUITABLE_BLAMER_FOUND);
        assertThat(log.getInfoMessages()).containsOnly(ACTUAL_FACTORY_NULL_BLAMER, EMPTY_FACTORY_NULL_BLAMER);
    }

    /** Verifies that the correct {@link Blamer} instance is created for the first repository. */
    @Test
    public void shouldSelectBlamerForFirstDirectory() {
        FilteredLog log = new FilteredLog("Foo");
        Blamer testBlamer = createBlamer("/test", log);

        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(ACTUAL_FACTORY_CREATED_A_BLAMER);

        assertThat(testBlamer).isInstanceOf(TestBlamer.class);
        assertThat(testBlamer.blame(new FileLocations(), log)).isNotEmpty();
        assertThat(testBlamer.blame(new FileLocations(), log)).hasFiles(FILE_NAME);
    }

    /**
     * Verifies that correct {@link Blamer} instance is created for the second repository. (The first repository does
     * return a {@link NullBlamer}.
     */
    @Test
    public void shouldSelectBlamerForSecondDirectory() {
        FilteredLog log = new FilteredLog("Foo");

        Collection<FilePath> directories = asSourceDirectories(createWorkspace("/"), createWorkspace("/test"));
        Blamer testBlamerSecondMatch = BlamerFactory.findBlamer(mock(Run.class), directories, TaskListener.NULL, log);
        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(EMPTY_FACTORY_NULL_BLAMER, ACTUAL_FACTORY_NULL_BLAMER,
                ACTUAL_FACTORY_CREATED_A_BLAMER);

        assertThat(testBlamerSecondMatch).isInstanceOf(TestBlamer.class);
        assertThat(testBlamerSecondMatch.blame(new FileLocations(), log)).isNotEmpty();
        assertThat(testBlamerSecondMatch.blame(new FileLocations(), log)).hasFiles(FILE_NAME);
    }

    private Blamer createBlamer(final String path, final FilteredLog log) {
        return BlamerFactory.findBlamer(mock(Run.class), asSourceDirectories(createWorkspace(path)),
                TaskListener.NULL, log);
    }

    /**
     * Factory that does not return a blamer.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class EmptyFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm, final Run<?, ?> run, final FilePath workspace,
                final TaskListener listener, final FilteredLog logger) {
            logger.logInfo("EmptyFactory returned NullBlamer");
            return Optional.empty();
        }
    }

    /**
     * Factory that returns a blamer if the workspace contains the String {@code test}.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class ActualFactory extends BlamerFactory {
        @Override
        public Optional<Blamer> createBlamer(final SCM scm, final Run<?, ?> run,
                final FilePath workspace, final TaskListener listener, final FilteredLog logger) {
            if (workspace.getRemote().contains("test")) {
                logger.logInfo("ActualFactory created a blamer");
                return Optional.of(new TestBlamer());
            }
            logger.logInfo("ActualFactory returned NullBlamer");
            return Optional.empty();
        }
    }

    /** A blamer for the test. */
    private static class TestBlamer extends Blamer {
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        public Blames blame(final FileLocations fileLocations, final FilteredLog logger) {
            Blames blames = new Blames();
            blames.add(new FileBlameBuilder().build(FILE_NAME));
            return blames;
        }
    }
}
