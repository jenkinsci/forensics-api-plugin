package io.jenkins.plugins.forensics.blame;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.forensics.blame.FileLocations.FileSystem;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link FileLocations}.
 *
 * @author Ullrich Hafner
 */
class FileLocationsTest {
    private static final String RELATIVE_PATH = "with/file.txt";
    private static final String WORKSPACE = "/absolute/path/to/workspace/";
    private static final String ABSOLUTE_PATH = WORKSPACE + RELATIVE_PATH;
    private static final String WINDOWS_WORKSPACE = "C:\\absolute\\path\\to\\workspace\\";
    private static final String WINDOWS_RELATIVE_PATH = "with/file.txt";
    private static final String WINDOWS_ABSOLUTE_PATH = "C:/absolute/path/to/workspace/" + WINDOWS_RELATIVE_PATH;
    private static final String ANOTHER_FILE = "another-file.txt";

    @Test
    void shouldCreateEmptyInstance() {
        FileLocations empty = createLocations(WORKSPACE);

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).hasNoRelativePaths();
        assertThat(empty).hasNoAbsolutePaths();
        assertThat(empty).hasNoErrorMessages();
        assertThat(empty).hasNoInfoMessages();
        assertThat(empty).hasNoSkippedFiles();

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.getLines(RELATIVE_PATH));

    }

    @Test
    void shouldCreateSingleBlame() {
        FileLocations locations = createLocations(WORKSPACE);

        locations.addLine(ABSOLUTE_PATH, 1);

        assertThat(locations).isNotEmpty();
        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).hasAbsolutePaths(ABSOLUTE_PATH);
        assertThat(locations).hasRelativePaths(RELATIVE_PATH);
        assertThat(locations.contains(RELATIVE_PATH)).isTrue();
        assertThat(locations.contains(ABSOLUTE_PATH)).isTrue();

        assertThat(locations.getLines(RELATIVE_PATH)).containsExactly(1);
        assertThat(locations.getLines(ABSOLUTE_PATH)).containsExactly(1);
        assertThat(locations).hasNoSkippedFiles();
    }

    @Test
    void shouldSkipBlameForFileNotInWorkspace() {
        FileLocations locations = createLocations(WORKSPACE);

        String expectedSkippedFile = "/somewhere-else/" + RELATIVE_PATH;
        locations.addLine(expectedSkippedFile, 1);

        assertThat(locations).isEmpty();
        assertThat(locations).hasSkippedFiles(expectedSkippedFile);
    }

    @Test
    void shouldConvertWindowsPathToUnix() {
        FileLocations locations = createLocations(WINDOWS_WORKSPACE);

        locations.addLine(WINDOWS_ABSOLUTE_PATH, 1);

        assertThat(locations).isNotEmpty();
        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).hasRelativePaths(RELATIVE_PATH);
        assertThat(locations.contains(RELATIVE_PATH)).isTrue();

        assertThat(locations.getLines(RELATIVE_PATH)).containsExactly(1);
    }

    @Test
    void shouldAddAdditionalLinesToRequest() {
        FileLocations locations = createLocations(WORKSPACE);

        locations.addLine(ABSOLUTE_PATH, 1);
        locations.addLine(ABSOLUTE_PATH, 2);

        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).hasRelativePaths(RELATIVE_PATH);
        assertThat(locations).hasAbsolutePaths(ABSOLUTE_PATH);

        assertThat(locations.getLines(RELATIVE_PATH)).containsExactly(1, 2);
        assertThat(locations.getLines(ABSOLUTE_PATH)).containsExactly(1, 2);
    }

    @Test
    void shouldCreateTwoDifferentBlamerInput() {
        FileLocations locations = createLocations(WORKSPACE);

        locations.addLine(ABSOLUTE_PATH, 1);
        locations.addLine(WORKSPACE + ANOTHER_FILE, 2);

        assertThat(locations.size()).isEqualTo(2);
        assertThat(locations).hasRelativePaths(RELATIVE_PATH, ANOTHER_FILE);

        assertThat(locations.getLines(RELATIVE_PATH)).containsExactly(1);
        assertThat(locations.getLines(ANOTHER_FILE)).containsExactly(2);

        String wrongFile = "wrong file";
        assertThatThrownBy(() -> locations.getLines(wrongFile))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(wrongFile);
    }

    private FileLocations createLocations(final String workspace) {
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.resolveAbsolutePath(anyString(), any())).thenReturn(workspace);
        return new FileLocations(workspace, fileSystem);
    }
}
