package io.jenkins.plugins.forensics.delta.model;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link Change}.
 *
 * @author Florian Orendi
 */
class ChangeTest {

    private static final ChangeEditType EDIT_TYPE = ChangeEditType.INSERT;
    private static final int CHANGED_FROM_LINE = 1;
    private static final int CHANGED_TO_LINE = 1;
    private static final int FROM_LINE = 1;
    private static final int TO_LINE = 3;

    @Test
    void shouldHaveWorkingGetters() {
        Change change = createChange();
        assertThat(change).hasEditType(EDIT_TYPE);
        assertThat(change).hasChangedFromLine(CHANGED_FROM_LINE);
        assertThat(change).hasChangedToLine(CHANGED_TO_LINE);
        assertThat(change).hasFromLine(FROM_LINE);
        assertThat(change).hasToLine(TO_LINE);
    }

    @Test
    void shouldInitializeChangedLinesPerDefault() {
        Change change = new Change(EDIT_TYPE, FROM_LINE, TO_LINE);
        assertThat(change).hasChangedFromLine(0);
        assertThat(change).hasChangedToLine(0);
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(Change.class).verify();
    }

    /**
     * Factory method which creates an instance of {@link Change}.
     *
     * @return the created instance
     */
    private Change createChange() {
        return new Change(EDIT_TYPE, CHANGED_FROM_LINE, CHANGED_TO_LINE, FROM_LINE, TO_LINE);
    }
}
