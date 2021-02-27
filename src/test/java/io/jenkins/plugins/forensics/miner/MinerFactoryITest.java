package io.jenkins.plugins.forensics.miner;

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

import io.jenkins.plugins.forensics.miner.FileStatistics.FileStatisticsBuilder;
import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static io.jenkins.plugins.util.PathStubs.*;
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

    /**
     * Verifies that a {@link NullMiner} is selected if the workspace is not a supported SCM.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldSelectNullMiner() throws InterruptedException {
        FilteredLog log = new FilteredLog("Foo");
        RepositoryMiner nullMiner = createMiner("/", log);

        assertThat(nullMiner).isInstanceOf(NullMiner.class);
        assertThat(nullMiner.mine(new RepositoryStatistics(), log)).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(
                "-> No suitable miner found.",
                "ActualFactory returned NullMiner",
                "EmptyFactory returned NullMiner");
        assertThat(log.getErrorMessages()).isEmpty();
    }

    /**
     * Verifies that the correct {@link RepositoryMiner} instance is created for the first repository.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldSelectMinerForFirstDirectory() throws InterruptedException {
        FilteredLog log = new FilteredLog("Foo");
        RepositoryMiner repositoryMiner = createMiner("/test", log);

        assertThat(repositoryMiner).isInstanceOf(TestMiner.class);
        assertThat(repositoryMiner.mine(new RepositoryStatistics(), log)).isNotEmpty();

        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly("ActualFactory created a miner");
    }

    private RepositoryMiner createMiner(final String path, final FilteredLog log) {
        return MinerFactory.findMiner(mock(SCM.class), mock(Run.class), createWorkspace(path),
                TaskListener.NULL, log);
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
            logger.logInfo("EmptyFactory returned NullMiner");
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
                logger.logInfo("ActualFactory created a miner");
                return Optional.of(new TestMiner());
            }
            logger.logInfo("ActualFactory returned NullMiner");
            return Optional.empty();
        }
    }

    /** A blamer for the test. */
    private static class TestMiner extends RepositoryMiner {
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        public RepositoryStatistics mine(final RepositoryStatistics previousStatistics, final FilteredLog logger) {
            RepositoryStatistics statistics = new RepositoryStatistics();
            statistics.add(new FileStatisticsBuilder().build("/file.txt"));
            statistics.addAll(previousStatistics);
            return statistics;
        }
    }
}
