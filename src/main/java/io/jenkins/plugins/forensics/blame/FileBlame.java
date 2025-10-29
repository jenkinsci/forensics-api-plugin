package io.jenkins.plugins.forensics.blame;

import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stores the repository blames for several lines of a single file. File names are stored using the absolute path of the
 * file.
 *
 * @author Ullrich Hafner
 */
public final class FileBlame implements Iterable<Integer>, Serializable {
    @Serial
    private static final long serialVersionUID = 7L; // release 0.7

    static final String EMPTY = "-";
    static final int EMPTY_INTEGER = 0;

    private final TreeString fileName;
    @SuppressWarnings("PMD.LooseCoupling") @CheckForNull // Deserialization of old format
    private HashMap<Integer, LineBlame> blamesByLine = new HashMap<>();

    /**
     * Creates a new instance of {@link FileBlame}.
     *
     * @param fileName
     *         the name of the file that should be blamed
     */
    private FileBlame(final TreeString fileName) {
        this.fileName = fileName;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @Serial
    @SuppressWarnings("DataFlowIssue")
    private Object readResolve() {
        if (timeByLine == null) {
            timeByLine = new HashMap<>();
        }
        if (blamesByLine == null) {
            blamesByLine = new HashMap<>();
            for (Integer line : lines) {
                var lineBlame = new LineBlame();
                lineBlame.setName(nameByLine.get(line));
                lineBlame.setEmail(emailByLine.get(line));
                lineBlame.setCommit(commitByLine.get(line));
                lineBlame.setAddedAt(timeByLine.getOrDefault(line, EMPTY_INTEGER));
                blamesByLine.put(line, lineBlame);
            }
        }
        return this;
    }

    public String getFileName() {
        return fileName.toString();
    }

    public Set<Integer> getLines() {
        return getBlamesByLine().keySet();
    }

    private Map<Integer, LineBlame> getBlamesByLine() {
        return Objects.requireNonNull(blamesByLine);
    }

    @Override
    @NonNull
    public Iterator<Integer> iterator() {
        return getLines().iterator();
    }

    private LineBlame getBlamesFor(final int lineNumber) {
        return getBlamesByLine().computeIfAbsent(lineNumber, k -> new LineBlame());
    }

    /**
     * Sets the commit ID for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param id
     *         the commit ID
     */
    public void setCommit(final int lineNumber, final String id) {
        getBlamesFor(lineNumber).setCommit(id);
    }

    /**
     * Returns the commit ID for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the commit ID
     */
    public String getCommit(final int line) {
        return getBlamesFor(line).getCommit();
    }

    /**
     * Sets the author name for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param name
     *         the author name
     */
    public void setName(final int lineNumber, final String name) {
        getBlamesFor(lineNumber).setName(name);
    }

    /**
     * Returns the author name for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the author name
     */
    public String getName(final int line) {
        return getBlamesFor(line).getName();
    }

    /**
     * Sets the email address for the specified line number.
     *
     * @param lineNumber
     *         the line number
     * @param emailAddress
     *         the email address of the author
     */
    public void setEmail(final int lineNumber, final String emailAddress) {
        getBlamesFor(lineNumber).setEmail(emailAddress);
    }

    /**
     * Returns the author email for the specified line.
     *
     * @param line
     *         the affected line
     *
     * @return the author email
     */
    public String getEmail(final int line) {
        return getBlamesFor(line).getEmail();
    }

    /**
     * Sets the modification time for the specified line. Essentially, this is the time of the last commit that changed
     * this line.
     *
     * @param lineNumber
     *         the line number
     * @param time
     *         the time of the commit (given as number of seconds since the standard base time known as "the epoch",
     *         namely January 1, 1970, 00:00:00 GMT).
     */
    public void setTime(final int lineNumber, final int time) {
        getBlamesFor(lineNumber).setAddedAt(time);
    }

    /**
     * Returns the modification time for the specified line. Essentially, this is the time of the last commit that
     * changed this line.
     *
     * @param line
     *         the affected line
     *
     * @return the time of the commit (given as number of seconds since the standard base time known as "the epoch",
     *         namely January 1, 1970, 00:00:00 GMT.).
     */
    public int getTime(final int line) {
        return getBlamesFor(line).getAddedAt();
    }

    /**
     * Merges the additional lines of the other {@link FileBlame} instance with the lines of this instance.
     *
     * @param other
     *         the other blames
     *
     * @throws IllegalArgumentException
     *         if the file name of the other instance does not match
     */
    public void merge(final FileBlame other) {
        if (other.getFileName().equals(getFileName())) {
            for (Integer otherLine : other) {
                if (!getBlamesByLine().containsKey(otherLine)) {
                    getBlamesByLine().put(otherLine, other.getBlamesFor(otherLine));
                }
            }
        }
        else {
            throw new IllegalArgumentException(
                    "File names must match! This instance: %s, other instance: %s".formatted(
                            getFileName(), other.getFileName()));
        }
    }

    @Override
    public String toString() {
        return fileName + " - " + lines;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var integers = (FileBlame) o;
        return fileName.equals(integers.fileName) && Objects.equals(blamesByLine, integers.blamesByLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, blamesByLine);
    }

    @SuppressWarnings("PMD.DataClass")
    private static class LineBlame implements Serializable {
        @Serial
        private static final long serialVersionUID = 7L; // release 0.7
        private String name = EMPTY;
        private String email = EMPTY;
        private String commit = EMPTY;
        private int addedAt = EMPTY_INTEGER;

        public String getName() {
            return name;
        }

        void setName(final String name) {
            this.name = name.intern();
        }

        public String getEmail() {
            return email;
        }

        void setEmail(final String email) {
            this.email = email.intern();
        }

        public String getCommit() {
            return commit;
        }

        void setCommit(final String commit) {
            this.commit = commit.intern();
        }

        public int getAddedAt() {
            return addedAt;
        }

        void setAddedAt(final int addedAt) {
            this.addedAt = addedAt;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var lineBlame = (LineBlame) o;
            return addedAt == lineBlame.addedAt
                    && name.equals(lineBlame.name)
                    && email.equals(lineBlame.email)
                    && commit.equals(lineBlame.commit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, email, commit, addedAt);
        }
    }

    /**
     * Creates {@link FileBlame} instances that optimize the memory footprint for file names by using a {@link
     * TreeStringBuilder}.
     */
    public static class FileBlameBuilder {
        private final TreeStringBuilder builder = new TreeStringBuilder();
        private final PathUtil pathUtil = new PathUtil();

        /**
         * Creates a new {@link FileBlame} instance for the specified file name. The file name will be normalized and
         * compressed using a {@link TreeStringBuilder}.
         *
         * @param fileName
         *         the file name
         * @return the created {@link FileBlame} instance
         */
        public FileBlame build(final String fileName) {
            return new FileBlame(builder.intern(pathUtil.getAbsolutePath(fileName)));
        }
    }

    @Deprecated
    @SuppressWarnings({"checkstyle:InnerTypeLast", "MismatchedQueryAndUpdateOfCollection"})
    private final transient Set<Integer> lines = new HashSet<>();
    @Deprecated
    @SuppressWarnings({"checkstyle:InnerTypeLast", "DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private final transient Map<Integer, String> commitByLine = new HashMap<>();
    @Deprecated
    @SuppressWarnings({"checkstyle:InnerTypeLast", "DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private final transient Map<Integer, String> nameByLine = new HashMap<>();
    @Deprecated
    @SuppressWarnings({"checkstyle:InnerTypeLast", "DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private final transient Map<Integer, String> emailByLine = new HashMap<>();
    @CheckForNull
    @Deprecated
    @SuppressWarnings({"checkstyle:InnerTypeLast", "DeprecatedIsStillUsed", "MismatchedQueryAndUpdateOfCollection"})
    private transient Map<Integer, Integer> timeByLine = new HashMap<>();
}
