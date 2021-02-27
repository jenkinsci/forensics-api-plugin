package io.jenkins.plugins.forensics.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.hm.hafner.util.FilteredLog;

import hudson.ExtensionPoint;
import hudson.model.Run;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.util.CommitDecorator.NullDecorator;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Jenkins extension point that allows plugins to create {@link CommitDecorator} instances based on a supported {@link
 * SCM} and {@link RepositoryBrowser}.
 *
 * @author Ullrich Hafner
 */
public abstract class CommitDecoratorFactory implements ExtensionPoint {
    private static final Function<Optional<CommitDecorator>, Stream<? extends CommitDecorator>> OPTIONAL_MAPPER
            = o -> o.map(Stream::of).orElseGet(Stream::empty);

    /**
     * Returns a commit decorator for the specified {@link SCM} and the associated {@link RepositoryBrowser}.
     *
     * @param scm
     *         the {@link SCM} to create the commit decorator for
     * @param logger
     *         a logger to report error messages
     *
     * @return a commit decorator for the specified {@link SCM} and the associated {@link RepositoryBrowser}
     */
    public abstract Optional<CommitDecorator> createCommitDecorator(SCM scm, FilteredLog logger);

    /**
     * Returns a commit decorator for the specified {@link SCM scm}.
     *
     * @param scm
     *         the SCM to get the decorator for
     * @param logger
     *         a logger to report error messages
     *
     * @return a commit decorator for the specified SCM or a {@link NullDecorator} if the SCM is not
     *         supported or if the SCM does not provide a {@link RepositoryBrowser} implementation
     */
    public static CommitDecorator findCommitDecorator(final SCM scm, final FilteredLog logger) {
        return findAllExtensions().stream()
                .map(factory -> factory.createCommitDecorator(scm, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst().orElse(new NullDecorator());
    }

    /**
     * Returns a commit decorator for the specified {@link Run build}.
     *
     * @param run
     *         the current build
     *
     * @return a commit decorator for the SCM of the specified build or a {@link NullDecorator} if the SCM is not
     *         supported or if the SCM does not provide a {@link RepositoryBrowser} implementation
     */
    public static CommitDecorator findCommitDecorator(final Run<?, ?> run) {
        return findCommitDecorator(new ScmResolver().getScm(run), new FilteredLog("ignored"));
    }

    private static List<CommitDecoratorFactory> findAllExtensions() {
        return new JenkinsFacade().getExtensionsFor(CommitDecoratorFactory.class);
    }
}
