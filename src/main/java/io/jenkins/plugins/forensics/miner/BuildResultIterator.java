package io.jenkins.plugins.forensics.miner;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.echarts.Build;
import io.jenkins.plugins.echarts.BuildResult;
import io.jenkins.plugins.util.BuildAction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BuildResultIterator<A extends BuildAction<R>, R> implements Iterator<BuildResult<R>> {
    private final Class<A> actionType;

    private Optional<A> latestAction;

    public BuildResultIterator(final Class<A> actionType, final Optional<A> latestAction) {
        this.actionType = actionType;
        this.latestAction = latestAction;
    }

    @Override
    public boolean hasNext() {
        return latestAction.isPresent();
    }

    @Override
    public BuildResult<R> next() {
        if (!latestAction.isPresent()) {
            throw new NoSuchElementException();
        }
        A buildAction = latestAction.get();
        Run<?, ?> run = buildAction.getOwner();
        latestAction = BuildAction.getBuildActionFromHistoryStartingFrom(run.getPreviousBuild(), actionType);

        return new BuildResult<>(new Build(run.getNumber(), run.getDisplayName(), (int)(run.getTimeInMillis() / 1000)), buildAction.getResult());
    }
}
