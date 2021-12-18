package io.jenkins.plugins.forensics.delta.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link FileChanges}.
 *
 * @author Florian Orendi
 */
class FileChangesTest {

    private static final String FILE_NAME = "test";
    private static final String FILE_CONTENT = "test";
    private static final FileEditType FILE_EDIT_TYPE = FileEditType.ADD;
    private static final Map<ChangeEditType, List<Change>> FILE_CHANGES = Collections.emptyMap();

    @Test
    void testFileChangesGetter() {
        final FileChanges fileChanges = createFileChanges();
        assertThat(fileChanges.getFileName()).isEqualTo(FILE_NAME);
        assertThat(fileChanges.getFileContent()).isEqualTo(FILE_CONTENT);
        assertThat(fileChanges.getFileEditType()).isEqualTo(FILE_EDIT_TYPE);
        assertThat(fileChanges.getChanges()).isEqualTo(FILE_CHANGES);
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(FileChanges.class).verify();
    }

    /**
     * Factory method which creates an instance of {@link FileChanges}.
     *
     * @return the created instance
     */
    private FileChanges createFileChanges() {
        return new FileChanges(FILE_NAME, FILE_CONTENT, FILE_EDIT_TYPE, FILE_CHANGES);
    }
}
