package io.jenkins.plugins.forensics.util;

import java.io.Serializable;
import java.util.List;

import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

/**
 * Facade to Jenkins server. Encapsulates all calls to the running Jenkins server so that tests can replace this facade
 * with a stub.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class JenkinsFacade implements Serializable {
    private static final long serialVersionUID = 1161185147211636402L;

    /**
     * Returns the discovered instances for the given extension type.
     *
     * @param extensionType
     *         The base type that represents the extension point. Normally {@link ExtensionPoint} subtype but that's not
     *         a hard requirement.
     * @param <T>
     *         type of the extension
     *
     * @return the discovered instances, might be an empty list
     */
    public <T> List<T> getExtensionsFor(final Class<T> extensionType) {
        return getJenkins().getExtensionList(extensionType);
    }

    private Jenkins getJenkins() {
        return Jenkins.get();
    }
}
