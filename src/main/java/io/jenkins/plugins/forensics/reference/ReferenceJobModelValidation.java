package io.jenkins.plugins.forensics.reference;

import edu.hm.hafner.util.VisibleForTesting;

import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Validates all properties of a configuration of a reference job.
 *
 * @author Ullrich Hafner
 */
class ReferenceJobModelValidation {
    private final JenkinsFacade jenkins;

    /** Creates a new descriptor. */
    ReferenceJobModelValidation() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    ReferenceJobModelValidation(final JenkinsFacade jenkins) {
        super();

        this.jenkins = jenkins;
    }

    /**
     * Returns the model with the possible reference jobs.
     *
     * @return the model with the possible reference jobs
     */
    public ComboBoxModel getAllJobs() {
        return new ComboBoxModel(jenkins.getAllJobNames());
    }

    /**
     * Performs on-the-fly validation of the reference job.
     *
     * @param referenceJobName
     *         the reference job
     *
     * @return the validation result
     */
    public FormValidation validateJob(final String referenceJobName) {
        if (jenkins.getJob(referenceJobName).isPresent()) {
            return FormValidation.ok();
        }
        return FormValidation.error(Messages.FieldValidator_Error_ReferenceJobDoesNotExist());
    }
}
