package io.jenkins.plugins.forensics.miner;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.echarts.Build;
import io.jenkins.plugins.echarts.BuildResult;
import io.jenkins.plugins.util.BuildAction;

/**
 * Iterates over a collection of builds that contain results of a given generic type. A new iterator starts from a
 * baseline build where it selects the attached action of the given type. From this action it obtains and returns the
 * current result. Then it moves back in the build history until no more builds are available.
 *
 * @param <A>
 *         type of the action that stores the result
 * @param <R>
 *         type of the result that is persisted within the action
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BuildResultIterator<A extends BuildAction<R>, R> implements Iterator<BuildResult<R>> {
    private final Class<A> actionType;

    private Optional<A> latestAction;

    /**
     * Creates a new iterator that selects action of the given type {@code actionType}.
     *
     * @param actionType
     *         the type of the actions to select
     * @param baseline
     *         the baseline to start from
     */
    public BuildResultIterator(final Class<A> actionType, final Optional<A> baseline) {
        this.actionType = actionType;
        this.latestAction = baseline;
    }

    @Override
    public boolean hasNext() {
        return latestAction.isPresent();
    }

    @Override
    public BuildResult<R> next() {
        if (!latestAction.isPresent()) {
            throw new NoSuchElementException("There is no action available anymore. Use hasNext() before calling next().");
        }

        A buildAction = latestAction.get();
        Run<?, ?> run = buildAction.getOwner();
        latestAction = BuildAction.getBuildActionFromHistoryStartingFrom(run.getPreviousBuild(), actionType);

        int buildTimeInSeconds = (int) (run.getTimeInMillis() / 1000);
        Build build = new Build(run.getNumber(), run.getDisplayName(), buildTimeInSeconds);
        return new BuildResult<>(build, buildAction.getResult());
    }
}
