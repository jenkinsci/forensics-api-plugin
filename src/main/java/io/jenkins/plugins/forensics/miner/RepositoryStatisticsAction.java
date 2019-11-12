package io.jenkins.plugins.forensics.miner;

import java.io.Serializable;
import java.util.Collection;

import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

/**
 * Controls the live cycle of the forensics results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the forensics details: these are rendered using a new view instance.
 *
 * @author Ullrich Hafner
 */
// TODO: Results are written to build.xml
// FIXME: FileStatistics contains today
public class RepositoryStatisticsAction implements LastBuildAction, RunAction2, ModelObject, Serializable {
    private static final long serialVersionUID = -2074456133028895573L;

    private final RepositoryStatistics repositoryStatistics;

    private transient Run<?, ?> owner;

    /**
     * Creates a new instance of {@link RepositoryStatisticsAction}.
     *
     * @param repositoryStatistics
     *         the statistics to persist with this action
     */
    public RepositoryStatisticsAction(final RepositoryStatistics repositoryStatistics) {
        this.repositoryStatistics = repositoryStatistics;
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        onAttached(r);
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return null;
    }

    public RepositoryStatistics getRepositoryStatistics() {
        return repositoryStatistics;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/forensics-api/icons/forensics-24x24.png";
    }

    @Override
    public String getDisplayName() {
        return "SCM Forensics";
    }

    @Override
    public String getUrlName() {
        return "forensics";
    }
}
