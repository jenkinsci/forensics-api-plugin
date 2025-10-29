package io.jenkins.plugins.forensics.delta;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.TestExtension;

import edu.hm.hafner.util.FilteredLog;

import java.io.Serial;
import java.util.Collection;
import java.util.Optional;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.delta.DeltaCalculator.NullDeltaCalculator;
import io.jenkins.plugins.util.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static io.jenkins.plugins.util.PathStubs.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link DeltaCalculatorFactory}.
 *
 * @author Florian Orendi
 */
class DeltaCalculatorFactoryITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String NO_SUITABLE_DELTA_CALCULATOR_FOUND = "-> No suitable delta calculator found.";
    private static final String ACTUAL_FACTORY_NULL_DELTA_CALCULATOR = "ActualFactory returned NullDeltaCalculator";
    private static final String EMPTY_FACTORY_NULL_DELTA_CALCULATOR = "EmptyFactory returned NullDeltaCalculator";
    private static final String ACTUAL_FACTORY_CREATED_A_DELTA_CALCULATOR = "ActualFactory created a DeltaCalculator";

    /** Verifies that a {@link NullDeltaCalculator} will be returned if no suitable delta calculator has been found. */
    @Test
    void shouldSelectNullDeltaCalculatorIfNoCalculatorIsFound() {
        var log = new FilteredLog("Foo");
        var nullCalculator = createDeltaCalculator("/", log);

        assertThat(nullCalculator).isInstanceOf(NullDeltaCalculator.class);
        assertThat(nullCalculator.calculateDelta(mock(Run.class), mock(Run.class), log)).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(NO_SUITABLE_DELTA_CALCULATOR_FOUND,
                ACTUAL_FACTORY_NULL_DELTA_CALCULATOR, EMPTY_FACTORY_NULL_DELTA_CALCULATOR);
        assertThat(log.getErrorMessages()).isEmpty();
    }

    /** Verifies that the correct {@link DeltaCalculator} instance is created for the first repository. */
    @Test
    void shouldSelectDeltaCalculatorForFirstDirectory() {
        var log = new FilteredLog("Foo");
        var deltaCalculator = createDeltaCalculator("/test", log);

        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(ACTUAL_FACTORY_CREATED_A_DELTA_CALCULATOR);

        assertThat(deltaCalculator).isInstanceOf(TestDeltaCalculator.class);
    }

    /**
     * Verifies that correct {@link DeltaCalculator} instance is created for the second repository. (The first
     * repository does return a {@link NullDeltaCalculator}.
     */
    @Test
    void shouldSelectDeltaCalculatorForSecondDirectory() {
        var log = new FilteredLog("Foo");

        Collection<FilePath> directories = asSourceDirectories(createWorkspace("/"), createWorkspace("/test"));
        DeltaCalculator testDeltaSecondMatch = DeltaCalculatorFactory.findDeltaCalculator(mock(Run.class), directories,
                TaskListener.NULL, log);
        assertThat(log.getErrorMessages()).isEmpty();
        assertThat(log.getInfoMessages()).containsOnly(EMPTY_FACTORY_NULL_DELTA_CALCULATOR,
                ACTUAL_FACTORY_NULL_DELTA_CALCULATOR,
                ACTUAL_FACTORY_CREATED_A_DELTA_CALCULATOR);

        assertThat(testDeltaSecondMatch).isInstanceOf(TestDeltaCalculator.class);
    }

    private DeltaCalculator createDeltaCalculator(final String path, final FilteredLog log) {
        return DeltaCalculatorFactory.findDeltaCalculator(mock(Run.class), asSourceDirectories(createWorkspace(path)),
                TaskListener.NULL, log);
    }

    /**
     * A delta calculator for the test.
     */
    private static class TestDeltaCalculator extends DeltaCalculator {
        @Serial
        private static final long serialVersionUID = -2091805649078555383L;

        @Override
        @SuppressWarnings("deprecation")
        public Optional<Delta> calculateDelta(final Run<?, ?> build, final Run<?, ?> referenceBuild,
                final String scmFilterKey, final FilteredLog logger) {
            return Optional.empty();
        }
    }

    /**
     * Factory that does not return a delta calculator.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class EmptyFactory extends DeltaCalculatorFactory {
        @Override
        public Optional<DeltaCalculator> createDeltaCalculator(final SCM scm, final Run<?, ?> run,
                final FilePath workspace,
                final TaskListener listener, final FilteredLog logger) {
            logger.logInfo(EMPTY_FACTORY_NULL_DELTA_CALCULATOR);
            return Optional.empty();
        }
    }

    /**
     * Factory that returns a delta calculator if the workspace contains the String {@code test}.
     */
    @TestExtension
    @SuppressWarnings("unused")
    public static class ActualFactory extends DeltaCalculatorFactory {
        @Override
        public Optional<DeltaCalculator> createDeltaCalculator(final SCM scm, final Run<?, ?> run,
                final FilePath workspace, final TaskListener listener, final FilteredLog logger) {
            if (workspace.getRemote().contains("test")) {
                logger.logInfo(ACTUAL_FACTORY_CREATED_A_DELTA_CALCULATOR);
                return Optional.of(new DeltaCalculatorFactoryITest.TestDeltaCalculator());
            }
            logger.logInfo(ACTUAL_FACTORY_NULL_DELTA_CALCULATOR);
            return Optional.empty();
        }
    }
}
