package io.jenkins.plugins.forensics.delta;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        assertThat(delta.getFileChangesMap()).isEqualTo(FILE_CHANGES_MAP);
    }

    @Test
    void shouldThrowExceptionWhenGettingFileChangesWithUnknownFileId() {
        Delta delta = createDelta();
        String fileId = "unknown";

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> delta.getFileChangesById(fileId))
                .withMessage(Delta.ERROR_MESSAGE_UNKNOWN_FILE, fileId);
    }

    @Test
    void shouldAddFileChanges() {
        Delta delta = createDelta();

        assertThat(delta.getFileChangesMap()).isEmpty();

        String fileIdOne = "id1";
        FileChanges fileChangesOne = Mockito.mock(FileChanges.class);
        FileChanges fileChangesTwo = Mockito.mock(FileChanges.class);

        delta.addFileChanges(fileIdOne, fileChangesOne);

        assertThat(delta.getFileChangesMap().size()).isEqualTo(1);
        assertThat(delta.getFileChangesMap()).containsKey(fileIdOne);
        assertThat(delta.getFileChangesById(fileIdOne)).isEqualTo(fileChangesOne);

        // adds different changes for the same file
        delta.addFileChanges(fileIdOne, fileChangesTwo);

        assertThat(delta.getFileChangesMap().size()).isEqualTo(1);
        assertThat(delta.getFileChangesMap()).containsKey(fileIdOne);
        assertThat(delta.getFileChangesById(fileIdOne)).isEqualTo(fileChangesTwo);
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
