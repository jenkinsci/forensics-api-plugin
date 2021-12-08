package io.jenkins.plugins.forensics.delta.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.assertThat;

/**
 * Tests the class {@link Change}.
 *
 * @author Florian Orendi
 */
class ChangeTest {

    private static final ChangeEditType EDIT_TYPE = ChangeEditType.INSERT;
    private static final int FROM_LINE = 1;
    private static final int TO_LINE = 3;

    private static Change change;

    @BeforeAll
    static void init() {
        change = new Change(EDIT_TYPE, FROM_LINE, TO_LINE);
    }

    @Test
    void testChangeGetters() {
        assertThat(change.getEditType()).isEqualTo(EDIT_TYPE);
        assertThat(change.getFromLine()).isEqualTo(FROM_LINE);
        assertThat(change.getToLine()).isEqualTo(TO_LINE);
    }

    @Test
    void testChangeSetters() {
        Change changeTwo = new Change(EDIT_TYPE, FROM_LINE, TO_LINE);

        assertThat(change).isEqualTo(changeTwo);
    }
}
