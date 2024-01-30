package io.jenkins.plugins.forensics.reference;

import java.util.Set;

import org.junit.jupiter.api.Test;

import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder.SimpleReferenceRecorderDescriptor;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleReferenceRecorderTest {
    @Test
    void shouldFillModel() {
        var jenkins = mock(JenkinsFacade.class);
        var job = mock(BuildableItem.class);

        var descriptor = new SimpleReferenceRecorderDescriptor(jenkins);

        assertThat(descriptor.doFillReferenceJobItems(job)).isEmpty();
        assertThat(descriptor.doFillRequiredResultItems(job)).isEmpty();

        var jobs = Set.of("one", "two");
        when(jenkins.getAllJobNames()).thenReturn(jobs);
        when(jenkins.hasPermission(Item.CONFIGURE, job)).thenReturn(true);

        assertThat(descriptor.doFillReferenceJobItems(job))
                .containsExactlyInAnyOrderElementsOf(jobs);
        assertThat(descriptor.doFillRequiredResultItems(job))
                .extracting("value")
                .containsExactlyInAnyOrder("FAILURE", "SUCCESS", "UNSTABLE");
    }

    @Test
    void shouldValidateJob() {
        var jenkins = mock(JenkinsFacade.class);
        var job = mock(BuildableItem.class);
        var model = mock(ReferenceJobModelValidation.class);

        when(jenkins.hasPermission(Item.CONFIGURE, job)).thenReturn(true);
        when(model.validateJob("one")).thenReturn(FormValidation.ok());
        when(model.validateJob("two")).thenReturn(FormValidation.error("error"));

        var descriptor = new SimpleReferenceRecorderDescriptor(jenkins, model);

        assertThat(descriptor.doCheckReferenceJob(job, "one").kind).isEqualTo(FormValidation.Kind.OK);
        assertThat(descriptor.doCheckReferenceJob(job, "two").kind).isEqualTo(Kind.ERROR);
    }
}
