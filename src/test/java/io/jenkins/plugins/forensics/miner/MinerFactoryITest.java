package io.jenkins.plugins.forensics.miner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.util.FilteredLog;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static io.jenkins.plugins.forensics.util.PathStubs.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link MinerFactory}.
 *
 * @author Ullrich Hafner
 */
public class MinerFactoryITest {
    /** Jenkins rule per suite. */
    @ClassRule
    public static final JenkinsRule JENKINS_PER_SUITE = new JenkinsRule();

    private static final FilteredLog LOG = new FilteredLog("Foo");

    /**
     * Verifies that different {@link RepositoryMiner} instances are created based on the stubbed workspace name.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldSelectMinerBasedOnWorkspaceName() throws InterruptedException {
        RepositoryMiner nullMiner = createMiner("/");

        assertThat(nullMiner).isInstanceOf(NullMiner.class);
        assertThat(nullMiner.mine(Collections.emptyList())).isEmpty();

        RepositoryMiner repositoryMiner = createMiner("/test");
        assertThat(repositoryMiner).isInstanceOf(TestMiner.class);
        assertThat(repositoryMiner.mine(Collections.emptyList())).isNotEmpty();
    }

    private RepositoryMiner createMiner(final String path) {
        return MinerFactory.findMiner(mock(Run.class), asSourceDirectories(createWorkspace(path)), TaskListener.NULL, LOG);
    }

    /**
     * Factory that does not return a blamer.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class EmptyFactory extends MinerFactory {
        @Override
        public Optional<RepositoryMiner> createMiner(final SCM scm, final Run<?, ?> run, final FilePath workspace,
                final TaskListener listener, final FilteredLog logger) {
            return Optional.empty();
        }
    }

    /**
     * Factory that returns a blamer if the workspace contains the String {@code test}.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class ActualFactory extends MinerFactory {
        @Override
        public Optional<RepositoryMiner> createMiner(final SCM scm, final Run<?, ?> run,
                final FilePath workspace, final TaskListener listener, final FilteredLog logger) {
            if (workspace.getRemote().contains("test")) {
                return Optional.of(new TestMiner());
            }
            return Optional.empty();
        }
    }

    /** A blamer for the test. */
    private static class TestMiner extends RepositoryMiner {
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        public RepositoryStatistics mine(final Collection<String> absoluteFileNames) {
            RepositoryStatistics statistics = new RepositoryStatistics();
            statistics.add(new FileStatistics("/file.txt"));
            return statistics;
        }
    }
}
