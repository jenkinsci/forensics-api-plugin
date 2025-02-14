package io.jenkins.plugins.forensics.delta;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldCreateEmptyChanges() {
        var fileChanges = createFileChanges();
        assertThat(fileChanges).hasFileName(FILE_NAME)
                .hasOldFileName(OLD_FILE_NAME)
                .hasFileContent(FILE_CONTENT)
                .hasFileEditType(FILE_EDIT_TYPE)
                .hasNoModifiedLines()
                .hasChanges(Map.of());
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(FileChanges.class).verify();
    }

    @Test
    void shouldAddChange() {
        var fileChanges = createFileChanges();

        var changeEditType = ChangeEditType.REPLACE;

        assertThat(fileChanges.getChanges()).isEmpty();
        assertThat(fileChanges.getChangesByType(changeEditType)).isEmpty();

        var first = createChange(changeEditType, 10, 14);
        fileChanges.addChange(first);
        var second = createChange(changeEditType, 100, 100);
        fileChanges.addChange(second);
        var unrelated = createChange(ChangeEditType.DELETE, 1000, 2000);
        fileChanges.addChange(unrelated);

        assertThat(fileChanges.getChanges()).containsOnly(
                entry(changeEditType, Set.of(first, second)),
                entry(ChangeEditType.DELETE, Set.of(unrelated)));
        assertThat(fileChanges.getChangesByType(changeEditType)).containsExactlyInAnyOrder(first, second);
        assertThat(fileChanges).hasModifiedLines(10, 11, 12, 13, 14, 100);
    }

    private Change createChange(final ChangeEditType changeEditType, final int start, final int end) {
        Change change = mock(Change.class);
        when(change.getEditType()).thenReturn(changeEditType);
        when(change.getFromLine()).thenReturn(start);
        when(change.getToLine()).thenReturn(end);
        return change;
    }

    private FileChanges createFileChanges() {
        return new FileChanges(FILE_NAME, OLD_FILE_NAME, FILE_CONTENT, FILE_EDIT_TYPE, Collections.emptyMap());
    }
}
