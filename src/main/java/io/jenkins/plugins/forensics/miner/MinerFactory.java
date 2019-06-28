package io.jenkins.plugins.forensics.miner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.blame.Blamer;
import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;
import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.util.FilteredLog;
import io.jenkins.plugins.forensics.util.JenkinsFacade;

/**
 * Jenkins extension point that allows plugins to create {@link Blamer} instances based on a supported {@link SCM}.
 *
 * @author Ullrich Hafner
 */
public abstract class MinerFactory implements ExtensionPoint {
    private static JenkinsFacade jenkinsFacade = new JenkinsFacade();

    @VisibleForTesting
    static void setJenkinsFacade(final JenkinsFacade facade) {
        jenkinsFacade = facade;
    }

    /**
     * Returns a repository miner for the specified {@link SCM}.
     *
     * @param scm
     *         the {@link SCM} to create the miner for
     * @param run
     *         the current build
     * @param workspace
     *         the workspace of the current build
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a repository miner instance that creates statistics for all available files in the specified {@link SCM}
     */
    public abstract Optional<RepositoryMiner> createMiner(SCM scm, Run<?, ?> run, FilePath workspace,
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
    public static RepositoryMiner findMinerFor(final SCM scm, final Run<?, ?> run, final FilePath workspace,
            final TaskListener listener, final FilteredLog logger) {
        return findAllExtensions().stream()
                .map(minerFactory -> minerFactory.createMiner(scm, run, workspace, listener, logger))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .findFirst()
                .orElse(new NullMiner());
    }

    private static List<MinerFactory> findAllExtensions() {
        return jenkinsFacade.getExtensionsFor(MinerFactory.class);
    }
}
