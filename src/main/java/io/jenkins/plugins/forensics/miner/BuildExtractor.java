package io.jenkins.plugins.forensics.miner;

import hudson.model.Run;

/**
 * Extracts information from the previous build.
 */
public class BuildExtractor {


    public static RepositoryStatistics previousBuildStatistics(final Run<?, ?> run) {
        Run<?, ?> previousBuild = run.getPreviousBuild();
        if (previousBuild == null) {
            return new RepositoryStatistics();
        }
        ForensicsBuildAction action = previousBuild.getAction(ForensicsBuildAction.class);
        return action.getResult();
    }
}
