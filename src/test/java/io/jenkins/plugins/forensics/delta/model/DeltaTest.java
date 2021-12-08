package io.jenkins.plugins.forensics.delta.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jenkins.plugins.forensics.assertions.Assertions.assertThat;

/**
 * Tests the class {@link Delta}.
 *
 * @author Florian Orendi
 */
class DeltaTest {

    private static final String CURRENT_COMMIT_ID = "testIdOne";
    private static final String REFERENCE_COMMIT_ID = "testIdTwo";

    private static Delta delta;

    @BeforeAll
    static void init() {
        delta = new Delta(CURRENT_COMMIT_ID, REFERENCE_COMMIT_ID);
    }

    @Test
    void testChangeGettersAndSetters() {
        String diffFileContent = "testContent";
        delta.setDiffFile(diffFileContent);
        Map<String, FileChanges> fileChanges = createFileChanges();
        delta.setFileChanges(fileChanges);

        assertThat(delta.getCurrentCommit()).isEqualTo(CURRENT_COMMIT_ID);
        assertThat(delta.getReferenceCommit()).isEqualTo(REFERENCE_COMMIT_ID);
        assertThat(delta.getDiffFile()).isEqualTo(diffFileContent);
        assertThat(delta.getFileChanges()).isEqualTo(fileChanges);
    }

    @Test
    void testChangeSetters() {
        Delta deltaTwo = new Delta(CURRENT_COMMIT_ID, REFERENCE_COMMIT_ID);

        assertThat(delta).isEqualTo(deltaTwo);
    }

    /**
     * Creates a map which contains {@link FileChanges} mapped by the file ID.
     *
     * @return the created map
     */
    private Map<String, FileChanges> createFileChanges() {
        ChangeEditType editType = ChangeEditType.INSERT;
        List<Change> changes = Collections.singletonList(new Change(editType, 1, 6));
        Map<ChangeEditType, List<Change>> changesMap = new HashMap<>();
        changesMap.put(editType, changes);
        Map<String, FileChanges> fileChangesMap = new HashMap<>();
        fileChangesMap.put("testKey", new FileChanges("test", "test", FileEditType.ADD, changesMap));
        return fileChangesMap;
    }
}
