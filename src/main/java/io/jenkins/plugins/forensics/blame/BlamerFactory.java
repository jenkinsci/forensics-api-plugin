package io.jenkins.plugins.forensics.blame;

import java.util.List;

import hudson.ExtensionPoint;
import hudson.scm.SCM;
import jenkins.model.Jenkins;

import io.jenkins.plugins.forensics.blame.Blamer.NullBlamer;

/**
 * Jenkins extension point that allows plugins to create {@link Blamer} instances based on a supported {@link SCM}.
 *
 * @author Ullrich Hafner
 */
public abstract class BlamerFactory implements ExtensionPoint {
    /**
     * Returns whether the specified {@link SCM} is supported by this factory.
     *
     * @param scm
     *         the {@link SCM} to check
     *
     * @return {@code true} if the {@link SCM} is supported by this factory, {@code false} otherwise
     */
    // FIXME: remove method and use optional
    public abstract boolean supports(SCM scm);

    /**
     * Returns a blamer for the specified {@link SCM}.
     *
     * @param scm
     *         the {@link SCM} to create the blamer for
     *
     * @return a blamer instance that can blame authors for the specified {@link SCM}
     */
    public abstract Blamer createBlamer(SCM scm);

    /**
     * Returns a blamer for the specified {@link SCM}.
     *
     * @param scm
     *         the {@link SCM} to create the blamer for
     *
     * @return a blamer for the specified SCM or a {@link NullBlamer} if no factory is supporting the specified {@link
     *         SCM}
     */
    public static Blamer findBlamerFor(final SCM scm) {
        return findAllExtensions(scm).stream()
                .filter(b -> b.supports(scm))
                .findFirst()
                .map(blamerFactory -> blamerFactory.createBlamer(scm))
                .orElse(new NullBlamer());
    }

    private static List<BlamerFactory> findAllExtensions(final SCM scm) {
        return Jenkins.get().getExtensionList(BlamerFactory.class);
    }

}
