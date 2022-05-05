package io.jenkins.plugins.forensics.delta.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link FileChanges}.
 *
 * @author Florian Orendi
 */
class FileChangesTest {

    private static final String FILE_NAME = "test";
    private static final String OLD_FILE_NAME = "testOld";
    private static final String FILE_CONTENT = "test";
    private static final FileEditType FILE_EDIT_TYPE = FileEditType.ADD;
    private static final Map<ChangeEditType, Set<Change>> FILE_CHANGES = Collections.emptyMap();

    @Test
    void testFileChangesGetter() {
        FileChanges fileChanges = createFileChanges();
        assertThat(fileChanges.getFileName()).isEqualTo(FILE_NAME);
        assertThat(fileChanges.getOldFileName()).isEqualTo(OLD_FILE_NAME);
        assertThat(fileChanges.getFileContent()).isEqualTo(FILE_CONTENT);
        assertThat(fileChanges.getFileEditType()).isEqualTo(FILE_EDIT_TYPE);
        assertThat(fileChanges.getChanges()).isEqualTo(FILE_CHANGES);
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(FileChanges.class).verify();
    }

    @Test
    void shouldAddChange() {
        FileChanges fileChanges = createFileChanges();

        ChangeEditType changeEditType = ChangeEditType.REPLACE;

        assertThat(fileChanges.getChanges()).isEmpty();
        assertThat(fileChanges.getChangesByType(changeEditType)).isEmpty();

        Change change = Mockito.mock(Change.class);
        Mockito.when(change.getEditType()).thenReturn(changeEditType);

        fileChanges.addChange(change);
        fileChanges.addChange(change);

        Map<ChangeEditType, Set<Change>> changesMap = fileChanges.getChanges();
        assertThat(changesMap.size()).isEqualTo(1);
        assertThat(changesMap).containsKey(changeEditType);

        Set<Change> changes = fileChanges.getChangesByType(changeEditType);
        assertThat(changes.size()).isEqualTo(1);
        assertThat(changes).contains(change);
    }

    /**
     * Factory method which creates an instance of {@link FileChanges}.
     *
     * @return the created instance
     */
    private FileChanges createFileChanges() {
        return new FileChanges(FILE_NAME, OLD_FILE_NAME, FILE_CONTENT, FILE_EDIT_TYPE, FILE_CHANGES);
    }
}
