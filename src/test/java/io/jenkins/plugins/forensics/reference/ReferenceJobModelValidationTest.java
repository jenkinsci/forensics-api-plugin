package io.jenkins.plugins.forensics.reference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation.Kind;

import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.reference.ReferenceRecorder.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReferenceJobModelValidation}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class ReferenceJobModelValidationTest {
    @Test
    void shouldHaveEmptyListModel() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobNames()).thenReturn(new HashSet<>());

        ReferenceJobModelValidation model = new ReferenceJobModelValidation(jenkins);

        assertThat(model.getAllJobs()).isEmpty();
    }

    @Test
    void shouldValidateToOkIfEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        ReferenceJobModelValidation model = new ReferenceJobModelValidation(jenkins);

        assertSoftly(softly -> {
            softly.assertThat(model.validateJob(NO_REFERENCE_JOB).kind).isEqualTo(Kind.ERROR);
            softly.assertThat(model.validateJob("").kind).isEqualTo(Kind.ERROR);
            softly.assertThat(model.validateJob(null).kind).isEqualTo(Kind.ERROR);
        });
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);

        Job<?, ?> job = mock(Job.class);
        String jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));

        ReferenceJobModelValidation model = new ReferenceJobModelValidation(jenkins);

        assertThat(model.validateJob(jobName).kind).isEqualTo(Kind.OK);
        assertThat(model.validateJob("other").kind).isEqualTo(Kind.ERROR);
    }


    @Test
    void shouldContainSingleElementAndPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        String name = "Job Name";
        when(jenkins.getFullNameOf(job)).thenReturn(name);
        when(jenkins.getAllJobNames()).thenReturn(Collections.singleton(name));

        ReferenceJobModelValidation model = new ReferenceJobModelValidation(jenkins);

        ComboBoxModel actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(1).containsExactly(name);
    }
}
