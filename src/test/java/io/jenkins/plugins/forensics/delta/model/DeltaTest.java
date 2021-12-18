package io.jenkins.plugins.forensics.delta.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private static Map<String, FileChanges> fileChangesMap = new HashMap<>();

    @Test
    void testDeltaGetters() {
        Delta delta = createDelta();
        assertThat(delta.getCurrentCommit()).isEqualTo(CURRENT_COMMIT_ID);
        assertThat(delta.getReferenceCommit()).isEqualTo(REFERENCE_COMMIT_ID);
        assertThat(delta.getFileChanges()).isEqualTo(fileChangesMap);
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
        ChangeEditType editType = ChangeEditType.INSERT;
        List<Change> changes = Collections.singletonList(new Change(editType, 1, 6));
        Map<ChangeEditType, List<Change>> changesMap = new HashMap<>();
        changesMap.put(editType, changes);
        fileChangesMap = new HashMap<>();
        fileChangesMap.put("testKey", new FileChanges("test", "test", FileEditType.ADD, changesMap));
        return new Delta(CURRENT_COMMIT_ID, REFERENCE_COMMIT_ID, fileChangesMap);
    }
}
