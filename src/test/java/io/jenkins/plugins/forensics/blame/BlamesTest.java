package io.jenkins.plugins.forensics.blame;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.util.SerializableTest;

import java.util.NoSuchElementException;

import io.jenkins.plugins.forensics.blame.FileBlame.FileBlameBuilder;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link Blames}.
 *
 * @author Ullrich Hafner
 */
class BlamesTest extends SerializableTest<Blames> {
    private static final String COMMIT = "commit";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final int TIME = 12_345;

    private static final String FILE_NAME = "file.txt";
    private static final String EMPTY = "-";
    private static final int EMPTY_TIME = 0;
    private static final String ANOTHER_FILE = "other.txt";

    @Test
    void shouldCreateEmptyInstance() {
        var empty = new Blames();

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
        assertThat(empty).hasNoFiles();

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> empty.getBlame(FILE_NAME));
    }

    @Test
    void shouldAddBlamesOfSingleFile() {
        var blames = new Blames();

        var fileBlame = createBlame(1, NAME, EMAIL, COMMIT, TIME);
        blames.add(fileBlame);

        assertThatBlamesContainsOneFile(blames);
        assertThat(blames.getBlame(FILE_NAME)).isEqualTo(fileBlame);

        var other = createBlame(2, NAME, EMAIL, COMMIT, TIME);
        blames.add(other);

        assertThatBlamesContainsOneFile(blames);

        assertThat(blames.getBlame(FILE_NAME)).hasFileName(FILE_NAME);
        assertThat(blames.getBlame(FILE_NAME)).hasLines(1, 2);

        var duplicate = createBlame(1, EMPTY, EMPTY, EMPTY, EMPTY_TIME);
        blames.add(duplicate);

        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames.getBlame(FILE_NAME).getName(1)).isEqualTo(NAME);
        assertThat(blames.getBlame(FILE_NAME).getEmail(1)).isEqualTo(EMAIL);
        assertThat(blames.getBlame(FILE_NAME).getCommit(1)).isEqualTo(COMMIT);
        assertThat(blames.getBlame(FILE_NAME).getTime(1)).isEqualTo(TIME);
    }

    @Test
    void shouldConcatenateWorkspacePath() {
        var blames = new Blames();

        var fileBlame = createBlame("file.txt", 1, NAME, EMAIL, COMMIT, TIME);
        blames.add(fileBlame);

        assertThat(blames.size()).isEqualTo(1);

        assertThat(blames).hasFiles(FILE_NAME);
        assertThat(blames.contains(FILE_NAME)).isTrue();
        assertThat(blames.getBlame(FILE_NAME)).isEqualTo(fileBlame);
    }

    @Test
    void shouldAddBlamesOfTwoFiles() {
        var blames = new Blames();

        var fileBlame = createBlame(FILE_NAME, 1, NAME, EMAIL, COMMIT, TIME);
        blames.add(fileBlame);
        var other = createBlame(ANOTHER_FILE, 2, NAME, EMAIL, COMMIT, TIME);
        blames.add(other);

        verifyBlamesOfTwoFiles(blames, fileBlame, other);
    }

    @Test
    void shouldMergeBlames() {
        var blames = new Blames();
        var fileBlame = createBlame(FILE_NAME, 1, NAME, EMAIL, COMMIT, TIME);
        blames.add(fileBlame);

        var otherBlames = new Blames();
        var other = createBlame(ANOTHER_FILE, 2, NAME, EMAIL, COMMIT, TIME);
        otherBlames.add(other);

        blames.addAll(otherBlames);

        verifyBlamesOfTwoFiles(blames, fileBlame, other);
    }

    private void verifyBlamesOfTwoFiles(final Blames blames, final FileBlame fileBlame, final FileBlame other) {
        assertThat(blames.size()).isEqualTo(2);
        assertThat(blames).hasFiles(FILE_NAME, ANOTHER_FILE);
        assertThat(blames.contains(FILE_NAME)).isTrue();
        assertThat(blames.contains(ANOTHER_FILE)).isTrue();
        assertThat(blames.getBlame(FILE_NAME)).isEqualTo(fileBlame);
        assertThat(blames.getBlame(ANOTHER_FILE)).isEqualTo(other);
    }

    private void assertThatBlamesContainsOneFile(final Blames blames) {
        assertThat(blames).isNotEmpty();
        assertThat(blames.size()).isEqualTo(1);
        assertThat(blames).hasFiles(FILE_NAME);
        assertThat(blames.contains(FILE_NAME)).isTrue();
    }

    private FileBlame createBlame(final int lineNumber, final String name, final String email, final String commit,
            final int time) {
        return createBlame(FILE_NAME, lineNumber, name, email, commit, time);
    }

    private FileBlame createBlame(final String fileName, final int lineNumber, final String name, final String email,
            final String commit, final int time) {
        var fileBlame = new FileBlameBuilder().build(fileName);
        fileBlame.setName(lineNumber, name);
        fileBlame.setCommit(lineNumber, commit);
        fileBlame.setEmail(lineNumber, email);
        fileBlame.setTime(lineNumber, time);
        return fileBlame;
    }

    @Override
    protected Blames createSerializable() {
        var blames = new Blames();

        var fileBlame = createBlame(1, NAME, EMAIL, COMMIT, TIME);
        blames.add(fileBlame);

        return blames;
    }
}
