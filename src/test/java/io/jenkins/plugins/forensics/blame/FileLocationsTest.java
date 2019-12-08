package io.jenkins.plugins.forensics.blame;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link FileLocations}.
 *
 * @author Ullrich Hafner
 */
class FileLocationsTest {
    private static final String RELATIVE_PATH = "with/file.txt";
    private static final String WORKSPACE = "/absolute/path/to/workspace/";
    private static final String ABSOLUTE_PATH = WORKSPACE + RELATIVE_PATH;
    private static final String ANOTHER_FILE = "another-file.txt";

    @Test
    void shouldCreateEmptyInstance() {
        FileLocations empty = new FileLocations();

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).hasNoFiles();

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.getLines(RELATIVE_PATH));
    }

    @Test
    void shouldCreateSingleBlame() {
        FileLocations locations = new FileLocations();

        locations.addLine(ABSOLUTE_PATH, 1);

        assertThat(locations).isNotEmpty();
        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).hasFiles(ABSOLUTE_PATH);
        assertThat(locations.contains(ABSOLUTE_PATH)).isTrue();
        assertThat(locations.getLines(ABSOLUTE_PATH)).containsExactly(1);
    }

    @Test
    void shouldAddAdditionalLinesToRequest() {
        FileLocations locations = new FileLocations();

        locations.addLine(ABSOLUTE_PATH, 1);
        locations.addLine(ABSOLUTE_PATH, 2);

        assertThat(locations.size()).isEqualTo(1);
        assertThat(locations).hasFiles(ABSOLUTE_PATH);

        assertThat(locations.getLines(ABSOLUTE_PATH)).containsExactly(1, 2);
    }

    @Test
    void shouldCreateTwoDifferentBlamerInput() {
        FileLocations locations = new FileLocations();

        locations.addLine(ABSOLUTE_PATH, 1);
        locations.addLine(ANOTHER_FILE, 2);

        assertThat(locations.size()).isEqualTo(2);
        assertThat(locations).hasFiles(ABSOLUTE_PATH, ANOTHER_FILE);

        assertThat(locations.getLines(ABSOLUTE_PATH)).containsExactly(1);
        assertThat(locations.getLines(ANOTHER_FILE)).containsExactly(2);

        String wrongFile = "wrong file";
        assertThatThrownBy(() -> locations.getLines(wrongFile))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(wrongFile);
    }
}
