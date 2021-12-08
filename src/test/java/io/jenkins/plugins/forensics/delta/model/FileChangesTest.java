package io.jenkins.plugins.forensics.delta.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jenkins.plugins.forensics.assertions.Assertions.assertThat;

/**
 * Tests the class {@link FileChanges}.
 *
 * @author Florian Orendi
 */
class FileChangesTest {

    private static final String FILE_NAME = "test";
    private static final String FILE_CONTENT = "test";
    private static final FileEditType FILE_EDIT_TYPE = FileEditType.ADD;

    private static Map<ChangeEditType, List<Change>> changesMap;

    private static FileChanges fileChanges;

    @BeforeAll
    static void init() {
        ChangeEditType editType = ChangeEditType.INSERT;
        List<Change> changes = Collections.singletonList(new Change(editType, 1, 6));
        changesMap = new HashMap<>();
        changesMap.put(editType, changes);
        fileChanges = new FileChanges(FILE_NAME, FILE_CONTENT, FILE_EDIT_TYPE, changesMap);
    }

    @Test
    void testFileChangesGetter() {
        assertThat(fileChanges.getFileName()).isEqualTo(FILE_NAME);
        assertThat(fileChanges.getFileContent()).isEqualTo(FILE_CONTENT);
        assertThat(fileChanges.getFileEditType()).isEqualTo(FILE_EDIT_TYPE);
        assertThat(fileChanges.getChanges()).isEqualTo(changesMap);
    }

    @Test
    void testFileChangesEquals() {
        FileChanges fileChangesTwo = new FileChanges(FILE_NAME, FILE_CONTENT, FILE_EDIT_TYPE, changesMap);
        assertThat(fileChanges).isEqualTo(fileChangesTwo);
    }
}
