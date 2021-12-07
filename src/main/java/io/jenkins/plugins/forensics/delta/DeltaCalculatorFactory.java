package io.jenkins.plugins.forensics.delta;

import edu.hm.hafner.util.FilteredLog;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import io.jenkins.plugins.forensics.delta.DeltaCalculator.NullDeltaCalculator;
import io.jenkins.plugins.forensics.util.ScmResolver;
import io.jenkins.plugins.util.JenkinsFacade;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Jenkins extension point that allows plugins to create {@link DeltaCalculator} instances based on a supported {@link
 * SCM}.
 *
 * @author Florian Orendi
 */
public abstract class DeltaCalculatorFactory implements ExtensionPoint {

    private static final Function<Optional<DeltaCalculator>, Stream<? extends DeltaCalculator>> OPTIONAL_MAPPER
            = o -> o.map(Stream::of).orElseGet(Stream::empty);

    /**
     * Returns a {@link DeltaCalculator} for the specified {@link Run build}.
     *
     * @param run            the current build
     * @param scmDirectories paths to search for the SCM repository
     * @param listener       a task listener
     * @param logger         a logger to report error messages
     * @return a delta calculator for the SCM of the specified build or a {@link NullDeltaCalculator} if the SCM is not
     * supported
     */
    public static DeltaCalculator findDeltaCalculator(final Run<?, ?> run,
                                                      final Collection<FilePath> scmDirectories, final TaskListener listener, final FilteredLog logger) {
        return scmDirectories.stream()
                .map(directory -> findDeltaCalculator(run, directory, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElseGet(() -> createNullDeltaCalculator(logger));
    }

    /**
     * Returns a {@link DeltaCalculator} for the specified {@link SCM repository}.
     *
     * @param scm      the key of the SCM repository (substring that must be part of the SCM key)
     * @param run      the current build
     * @param workTree the working tree of the repository
     * @param listener a task listener
     * @param logger   a logger to report error messages
     * @return a delta calculator for the SCM of the specified build or a {@link NullDeltaCalculator} if the SCM is not
     * supported
     */
    public static DeltaCalculator findDeltaCalculator(final String scm, final Run<?, ?> run,
                                                      final FilePath workTree, final TaskListener listener, final FilteredLog logger) {
        Collection<? extends SCM> scms = new ScmResolver().getScms(run, scm);
        if (scms.isEmpty()) {
            logger.logInfo("-> no SCMs found to be processed");
            return new NullDeltaCalculator();
        }
        return findAllDeltaCalculatorFactoryInstances().stream()
                .map(deltaCalculatorFactory -> deltaCalculatorFactory.createDeltaCalculator(scms.iterator().next(), run,
                        workTree, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElseGet(() -> createNullDeltaCalculator(logger));
    }

    /**
     * Returns a {@link DeltaCalculator} for the specified {@link Run build} and {@link FilePath}.
     *
     * @param run      the current build
     * @param workTree the working tree of the repository
     * @param listener a task listener
     * @param logger   a logger to report error messages
     * @return a delta calculator for the SCM of the specified build or a {@link NullDeltaCalculator} if the SCM is not
     * supported
     */
    private static Optional<DeltaCalculator> findDeltaCalculator(final Run<?, ?> run, final FilePath workTree,
                                                                 final TaskListener listener, final FilteredLog logger) {
        SCM scm = new ScmResolver().getScm(run);
        return findAllDeltaCalculatorFactoryInstances().stream()
                .map(deltaCalculatorFactory -> deltaCalculatorFactory.createDeltaCalculator(scm, run, workTree,
                        listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst();
    }

    /**
     * Creates a {@link NullDeltaCalculator} that can be used if no installed delta calculator has been found.
     *
     * @param logger The log
     * @return the created delta calculator instance
     */
    private static DeltaCalculator createNullDeltaCalculator(final FilteredLog logger) {
        if (findAllDeltaCalculatorFactoryInstances().isEmpty()) {
            logger.logInfo(
                    "-> No delta calculator installed yet. "
                            + "You need to install the 'git-forensics' plugin to enable it for Git.");
        }
        else {
            logger.logInfo("-> No suitable delta calculator found.");
        }
        return new NullDeltaCalculator();
    }

    /**
     * Searches for all installed Jenkins extensions for {@link DeltaCalculatorFactory}.
     *
     * @return all found extensions
     */
    private static List<DeltaCalculatorFactory> findAllDeltaCalculatorFactoryInstances() {
        return new JenkinsFacade().getExtensionsFor(DeltaCalculatorFactory.class);
    }

    /**
     * Returns a {@link DeltaCalculator} for the specified {@link SCM}.
     *
     * @param scm       the {@link SCM} to create the delta calculator for
     * @param run       the current build
     * @param workspace the workspace of the current build
     * @param listener  a task listener
     * @param logger    a logger to report error messages
     * @return a matching delta calculator instance
     */
    public abstract Optional<DeltaCalculator> createDeltaCalculator(SCM scm, Run<?, ?> run, FilePath workspace,
                                                                    TaskListener listener, FilteredLog logger);
}
