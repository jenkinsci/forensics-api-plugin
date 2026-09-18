package io.jenkins.plugins.forensics.reference;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.util.Arrays;
import java.util.Optional;

import hudson.model.Action;
import hudson.model.Run;

/**
 * Filters builds by the {@link Action actions} they provide. An action matches if its class name equals the required
 * type (the fully qualified or the simple name of the action, one of its supertypes or one of its interfaces) and if
 * its {@link Action#getUrlName() URL name} equals the required ID. Both criteria are optional: an empty value matches
 * every action.
 *
 * @author Akash Manna
 */
class ActionFilter {
    private final String type;
    private final String id;

    ActionFilter(final String type, final String id) {
        this.type = StringUtils.stripToEmpty(type);
        this.id = StringUtils.stripToEmpty(id);
    }

    boolean isEnabled() {
        return StringUtils.isNotBlank(type) || StringUtils.isNotBlank(id);
    }

    /**
     * Returns whether the specified build provides a matching action.
     *
     * @param build
     *         the build to check
     *
     * @return {@code true} if the build provides a matching action or if this filter is disabled
     */
    boolean accepts(final Run<?, ?> build) {
        return !isEnabled() || build.getAllActions().stream().anyMatch(this::matches);
    }

    /**
     * Returns the first build that provides a matching action, starting with the specified build and continuing with
     * its predecessors.
     *
     * @param start
     *         the first build to check
     *
     * @return the matching build (or empty if no such build exists)
     */
    Optional<Run<?, ?>> findFirstAcceptedBuild(final Run<?, ?> start) {
        for (Run<?, ?> build = start; build != null; build = build.getPreviousCompletedBuild()) {
            if (accepts(build)) {
                return Optional.of(build);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a suffix that can be appended to a log message to describe the requirement of this filter.
     *
     * @return the suffix, or an empty string if this filter is disabled
     */
    String getRequirementSuffix() {
        return isEnabled() ? " and provide an action %s".formatted(this) : StringUtils.EMPTY;
    }

    private boolean matches(final Action action) {
        return (StringUtils.isBlank(type) || isOfRequiredType(action.getClass()))
                && (StringUtils.isBlank(id) || id.equals(action.getUrlName()));
    }

    private boolean isOfRequiredType(@CheckForNull final Class<?> candidate) {
        if (candidate == null) {
            return false;
        }
        return type.equals(candidate.getName())
                || type.equals(candidate.getSimpleName())
                || isOfRequiredType(candidate.getSuperclass())
                || Arrays.stream(candidate.getInterfaces()).anyMatch(this::isOfRequiredType);
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(id)) {
            return "of type '%s'".formatted(type);
        }
        if (StringUtils.isBlank(type)) {
            return "with ID '%s'".formatted(id);
        }
        return "of type '%s' with ID '%s'".formatted(type, id);
    }
}
