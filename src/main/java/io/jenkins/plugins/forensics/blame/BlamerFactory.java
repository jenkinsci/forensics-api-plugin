package io.jenkins.plugins.forensics.blame;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.hm.hafner.util.FilteredLog;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.util.ScmResolver;

/**
 * Jenkins extension point that allows plugins to create {@link Blamer} instances based on a supported {@link SCM}.
 *
 * @author Ullrich Hafner
 */
public abstract class BlamerFactory implements ExtensionPoint {
    private static final Function<Optional<Blamer>, Stream<? extends Blamer>> OPTIONAL_MAPPER
            = o -> o.map(Stream::of).orElseGet(Stream::empty);

    /**
     * Returns a blamer for the specified {@link SCM}.
     *
     * @param scm
     *         the {@link SCM} to create the blamer for
     * @param run
     *         the current build
     * @param workspace
     *         the workspace of the current build
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a blamer instance that can blame authors for the specified {@link SCM}
     */
    public abstract Optional<Blamer> createBlamer(SCM scm, Run<?, ?> run, FilePath workspace,
            TaskListener listener, FilteredLog logger);

    /**
     * Returns a blamer for the specified {@link Run build}.
     *
     * @param run
     *         the current build
     * @param scmDirectories
     *         paths to search for the SCM repository
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a blamer for the SCM of the specified build or a {@link NullBlamer} if the SCM is not supported
     */
    public static Blamer findBlamer(final Run<?, ?> run,
            final Collection<FilePath> scmDirectories, final TaskListener listener, final FilteredLog logger) {
        return scmDirectories.stream()
                .map(directory -> findBlamer(run, directory, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElse(new NullBlamer());
    }

    private static Optional<Blamer> findBlamer(final Run<?, ?> run, final FilePath workTree,
            final TaskListener listener, final FilteredLog logger) {
        SCM scm = new ScmResolver().getScm(run);

        return findAllExtensions().stream()
                .map(blamerFactory -> blamerFactory.createBlamer(scm, run, workTree, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst();
    }

    private static List<BlamerFactory> findAllExtensions() {
        return Jenkins.get().getExtensionList(BlamerFactory.class);
    }
}
