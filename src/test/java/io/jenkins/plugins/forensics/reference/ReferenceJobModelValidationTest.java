package io.jenkins.plugins.forensics.reference;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import hudson.model.Job;
import hudson.util.FormValidation.Kind;

import io.jenkins.plugins.util.JenkinsFacade;

import static org.assertj.core.api.Assertions.*;
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

        var model = new ReferenceJobModelValidation(jenkins);

        assertThat(model.getAllJobs()).isEmpty();
    }

    @Test
    void shouldValidateToOkIfEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        var model = new ReferenceJobModelValidation(jenkins);

        assertThat(model.validateJob("").kind).isEqualTo(Kind.OK);
        assertThat(model.validateJob(null).kind).isEqualTo(Kind.OK);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);

        Job<?, ?> job = mock(Job.class);
        var jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));

        var model = new ReferenceJobModelValidation(jenkins);

        assertThat(model.validateJob(jobName).kind).isEqualTo(Kind.OK);
        assertThat(model.validateJob("other").kind).isEqualTo(Kind.ERROR);
    }

    @Test
    void shouldContainSingleElementAndPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        var name = "Job Name";
        when(jenkins.getFullNameOf(job)).thenReturn(name);
        when(jenkins.getAllJobNames()).thenReturn(Set.of(name));

        var model = new ReferenceJobModelValidation(jenkins);

        var actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(1).containsExactly(name);
    }
}
