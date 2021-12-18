package io.jenkins.plugins.forensics.delta.model;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link Delta}.
 *
 * @author Florian Orendi
 */
class DeltaTest {

    private static final String CURRENT_COMMIT_ID = "testIdOne";
    private static final String REFERENCE_COMMIT_ID = "testIdTwo";
    private static final Map<String, FileChanges> FILE_CHANGES_MAP = Collections.emptyMap();

    @Test
    void testDeltaGetters() {
        Delta delta = createDelta();
        assertThat(delta.getCurrentCommit()).isEqualTo(CURRENT_COMMIT_ID);
        assertThat(delta.getReferenceCommit()).isEqualTo(REFERENCE_COMMIT_ID);
        assertThat(delta.getFileChanges()).isEqualTo(FILE_CHANGES_MAP);
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(Delta.class).verify();
    }

    /**
     * Factory method which creates an instance of {@link Delta}.
     *
     * @return the created instance
     */
    private Delta createDelta() {
        return new Delta(CURRENT_COMMIT_ID, REFERENCE_COMMIT_ID, FILE_CHANGES_MAP);
    }
}
