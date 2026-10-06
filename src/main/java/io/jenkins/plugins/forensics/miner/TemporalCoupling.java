package io.jenkins.plugins.forensics.miner;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Stores the temporal coupling of a pair of repository files. Two files are temporally coupled if they are frequently
 * modified together within the same commit: such a coupling reveals a hidden dependency between these files that is
 * not visible in the source code itself. See "Your Code as a Crime Scene" by Adam Tornhill, page 72, for details.
 *
 * <p>
 * This model is independent of the actual SCM implementation, so every SCM plugin can compute and store these
 * couplings in the {@link RepositoryStatistics} of a build.
 * </p>
 *
 * @author Akash Manna
 */
public final class TemporalCoupling implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // since 1.9.0

    private static final double PERCENTAGE_FACTOR = 100.0;
    private static final double ROUNDING_FACTOR = 10.0;

    private final String leftFile;
    private final String rightFile;
    private final int coChanges;
    private final double couplingRatio;

    /**
     * Creates a new instance of {@link TemporalCoupling}.
     *
     * @param leftFile
     *         the absolute path of the first file of this coupling
     * @param rightFile
     *         the absolute path of the second file of this coupling
     * @param coChanges
     *         the number of commits that changed both files
     * @param couplingRatio
     *         the strength of the coupling in the interval {@code [0.0, 1.0]}: it is defined as the number of shared
     *         commits divided by the smaller number of total commits of both files
     */
    public TemporalCoupling(final String leftFile, final String rightFile, final int coChanges,
            final double couplingRatio) {
        this.leftFile = leftFile;
        this.rightFile = rightFile;
        this.coChanges = coChanges;
        this.couplingRatio = couplingRatio;
    }

    /**
     * Returns the absolute path of the first file of this coupling.
     *
     * @return the path of the first file
     */
    public String getLeftFile() {
        return leftFile;
    }

    /**
     * Returns the absolute path of the second file of this coupling.
     *
     * @return the path of the second file
     */
    public String getRightFile() {
        return rightFile;
    }

    /**
     * Returns the number of commits that changed both files of this coupling.
     *
     * @return the number of shared commits
     */
    public int getCoChanges() {
        return coChanges;
    }

    /**
     * Returns the strength of this coupling in the interval {@code [0.0, 1.0]}. A value of {@code 1.0} means that both
     * files have always been changed together.
     *
     * @return the coupling ratio
     */
    public double getCouplingRatio() {
        return couplingRatio;
    }

    /**
     * Returns the strength of this coupling in the interval {@code [0.0, 100.0]}, rounded to one decimal place.
     *
     * @return the coupling ratio as percentage
     */
    public double getCouplingPercentage() {
        return Math.round(couplingRatio * PERCENTAGE_FACTOR * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Returns whether the specified file is part of this coupling.
     *
     * @param fileName
     *         the absolute path of the file to check
     *
     * @return {@code true} if the file is the left or right file of this coupling, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return Objects.equals(leftFile, fileName) || Objects.equals(rightFile, fileName);
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (TemporalCoupling) o;
        return coChanges == that.coChanges
                && Double.compare(couplingRatio, that.couplingRatio) == 0
                && Objects.equals(leftFile, that.leftFile)
                && Objects.equals(rightFile, that.rightFile);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(leftFile, rightFile, coChanges, couplingRatio);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", TemporalCoupling.class.getSimpleName() + "[", "]")
                .add("leftFile=" + leftFile)
                .add("rightFile=" + rightFile)
                .add("coChanges=" + coChanges)
                .add("couplingRatio=" + couplingRatio)
                .toString();
    }
}
