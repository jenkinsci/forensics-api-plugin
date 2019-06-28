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

import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.util.FilteredLog;
import io.jenkins.plugins.forensics.util.JenkinsFacade;
import io.jenkins.plugins.forensics.util.ScmResolver;

/**
 * Jenkins extension point that allows plugins to create {@link RepositoryMiner} instances based on a supported {@link
 * SCM}.
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
     * Returns a miner for the repository of the specified {@link Run build}.
     *
     * @param run
     *         the current build
     * @param workspace
     *         the workspace of the current build
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a miner for the SCM of the specified build or a {@link NullMiner} if the SCM is not supported
     */
    public static RepositoryMiner findMinerFor(final Run<?, ?> run, final FilePath workspace,
            final TaskListener listener, final FilteredLog logger) {
        SCM scm = new ScmResolver().getScm(run);

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
