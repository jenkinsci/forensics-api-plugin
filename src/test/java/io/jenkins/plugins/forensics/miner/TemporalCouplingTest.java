package io.jenkins.plugins.forensics.miner;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

/**
 * Tests the class {@link TemporalCoupling}.
 *
 * @author Akash Manna
 */
class TemporalCouplingTest {
    private static final String LEFT_FILE = "src/main/java/Left.java";
    private static final String RIGHT_FILE = "src/main/java/Right.java";
    private static final int CO_CHANGES = 12;
    private static final double COUPLING_RATIO = 0.75;

    @Test
    void shouldCreateTemporalCoupling() {
        var coupling = createCoupling();

        assertThat(coupling)
                .hasLeftFile(LEFT_FILE)
                .hasRightFile(RIGHT_FILE)
                .hasCoChanges(CO_CHANGES)
                .hasCouplingRatio(COUPLING_RATIO)
                .hasCouplingPercentage(75.0);
    }

    @Test
    void shouldRoundCouplingPercentageToOneDecimalPlace() {
        var coupling = new TemporalCoupling(LEFT_FILE, RIGHT_FILE, 2, 2.0 / 3.0);

        assertThat(coupling).hasCouplingPercentage(66.7);
    }

    @Test
    void shouldHandleTheBoundsOfTheCouplingRatio() {
        assertThat(new TemporalCoupling(LEFT_FILE, RIGHT_FILE, 0, 0)).hasCouplingPercentage(0);
        assertThat(new TemporalCoupling(LEFT_FILE, RIGHT_FILE, CO_CHANGES, 1)).hasCouplingPercentage(100.0);
    }

    @Test
    void shouldFindParticipatingFiles() {
        var coupling = createCoupling();

        assertThat(coupling.contains(LEFT_FILE)).isTrue();
        assertThat(coupling.contains(RIGHT_FILE)).isTrue();
        assertThat(coupling.contains("src/main/java/Other.java")).isFalse();
    }

    @Test
    void shouldObeyEqualsContract() {
        EqualsVerifier.simple().forClass(TemporalCoupling.class).verify();
    }

    @Test
    void shouldProvideToString() {
        assertThat(createCoupling()).hasToString(
                "TemporalCoupling[leftFile=%s, rightFile=%s, coChanges=%d, couplingRatio=%s]".formatted(
                        LEFT_FILE, RIGHT_FILE, CO_CHANGES, COUPLING_RATIO));
    }

    private TemporalCoupling createCoupling() {
        return new TemporalCoupling(LEFT_FILE, RIGHT_FILE, CO_CHANGES, COUPLING_RATIO);
    }
}
