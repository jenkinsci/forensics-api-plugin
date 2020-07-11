package io.jenkins.plugins.forensics.miner;

import java.util.Collection;
import java.util.Collections;
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

    private static final String NO_SUITABLE_MINER_FOUND = "-> No suitable miner found.";
    private static final String EMPTY_FACTORY_NULL_MINER = "EmptyFactory returned NullMiner";
    private static final String ACTUAL_FACTORY_NULL_MINER = "ActualFactory returned NullMiner";
    private static final String ACTUAL_FACTORY_CREATED_A_MINER = "ActualFactory created a miner";

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
        assertThat(nullMiner.mine(Collections.emptyList(), log)).isEmpty();
        assertThat(log.getErrorMessages()).contains(NO_SUITABLE_MINER_FOUND);
        assertThat(log.getInfoMessages()).containsOnly(ACTUAL_FACTORY_NULL_MINER, EMPTY_FACTORY_NULL_MINER);
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
        assertThat(repositoryMiner.mine(Collections.emptyList(), log)).isNotEmpty();

        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(ACTUAL_FACTORY_CREATED_A_MINER);
    }

    /**
     * Verifies that correct {@link RepositoryMiner} instance is created for the second repository. (The first
     * repository does return a {@link NullMiner}.
     *
     * @throws InterruptedException
     *         never thrown
     */
    @Test
    public void shouldSelectMinerBasedOnEmptyFactory() {
        FilteredLog log = new FilteredLog("Foo");

        Collection<FilePath> directories = asSourceDirectories(createWorkspace("/"), createWorkspace("/test"));
        RepositoryMiner testMinerSecondMatch = MinerFactory.findMiner(mock(Run.class), directories, TaskListener.NULL,
                log);
        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(EMPTY_FACTORY_NULL_MINER, ACTUAL_FACTORY_NULL_MINER,
                ACTUAL_FACTORY_CREATED_A_MINER);

        assertThat(testMinerSecondMatch).isInstanceOf(TestMiner.class);
    }

    private RepositoryMiner createMiner(final String path, final FilteredLog log) {
        return MinerFactory.findMiner(mock(Run.class), asSourceDirectories(createWorkspace(path)),
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
        public RepositoryStatistics mine(final Collection<String> absoluteFileNames,
                final FilteredLog logger) {
            RepositoryStatistics statistics = new RepositoryStatistics();
            statistics.add(new FileStatisticsBuilder().build("/file.txt"));
            return statistics;
        }
    }
}
