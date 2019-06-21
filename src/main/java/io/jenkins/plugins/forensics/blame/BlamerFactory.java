package io.jenkins.plugins.forensics.blame;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.util.FilteredLog;
import io.jenkins.plugins.forensics.util.JenkinsFacade;

/**
 * Jenkins extension point that allows plugins to create {@link Blamer} instances based on a supported {@link SCM}.
 *
 * @author Ullrich Hafner
 */
public abstract class BlamerFactory implements ExtensionPoint {
    private static JenkinsFacade jenkinsFacade = new JenkinsFacade();

    @VisibleForTesting
    static void setJenkinsFacade(final JenkinsFacade facade) {
        jenkinsFacade = facade;
    }

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
     * @return a blamer for the specified SCM or a {@link NullBlamer} if no factory is supporting the specified {@link
     *         SCM}
     */
    public static Blamer findBlamerFor(final SCM scm, final Run<?, ?> run, final FilePath workspace,
            final TaskListener listener, final FilteredLog logger) {
        return findAllExtensions().stream()
                .map(blamerFactory -> blamerFactory.createBlamer(scm, run, workspace, listener, logger))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .findFirst()
                .orElse(new NullBlamer());
    }

    private static List<BlamerFactory> findAllExtensions() {
        return jenkinsFacade.getExtensionsFor(BlamerFactory.class);
    }
}
