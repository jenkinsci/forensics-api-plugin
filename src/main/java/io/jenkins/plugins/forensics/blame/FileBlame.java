package io.jenkins.plugins.forensics.blame;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Stores the repository blames for several lines of a single file. File names are stored using the absolute path of the
 * file.
 *
 * @author Ullrich Hafner
 */
public class FileBlame implements Iterable<Integer>, Serializable {
    private static final long serialVersionUID = -7491390234189584964L;

    private static final String UNIX_SLASH = "/";
    private static final String WINDOWS_BACK_SLASH = "\\";

    static final String EMPTY = "-";
    static final int EMPTY_INTEGER = 0;

    private final String fileName;
    private final Set<Integer> lines = new HashSet<>();

    private final Map<Integer, String> commitByLine = new HashMap<>();
    private final Map<Integer, String> nameByLine = new HashMap<>();
    private final Map<Integer, String> emailByLine = new HashMap<>();
    @Nullable
    private Map<Integer, Integer> timeByLine = new HashMap<>();

    /**
     * Creates a new instance of {@link FileBlame}.
     *
     * @param fileName
     *         the name of the file that should be blamed
     */
    public FileBlame(final String fileName) {
        this.fileName = StringUtils.replace(fileName, WINDOWS_BACK_SLASH, UNIX_SLASH);
    }

    public String getFileName() {
        return fileName;
    }

    public Set<Integer> getLines() {
        return lines;
    }

    @Override
    @NonNull
    public Iterator<Integer> iterator() {
        return lines.iterator();
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
        setInternedStringValue(commitByLine, lineNumber, id);
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
        return getStringValue(commitByLine, line);
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
        setInternedStringValue(nameByLine, lineNumber, name);
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
        return getStringValue(nameByLine, line);
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
        setInternedStringValue(emailByLine, lineNumber, emailAddress);
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
        return getStringValue(emailByLine, line);
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
        setIntegerValue(timeByLine, lineNumber, time);
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
        return getIntegerValue(timeByLine, line);
    }

    private String getStringValue(final Map<Integer, String> map, final int line) {
        if (map.containsKey(line)) {
            return map.get(line);
        }
        return EMPTY;
    }

    private void setInternedStringValue(final Map<Integer, String> map, final int lineNumber, final String value) {
        map.put(lineNumber, value.intern());
        lines.add(lineNumber);
    }

    private int getIntegerValue(final Map<Integer, Integer> map, final int line) {
        return map.getOrDefault(line, EMPTY_INTEGER);
    }

    private void setIntegerValue(final Map<Integer, Integer> map, final int lineNumber, final Integer value) {
        map.put(lineNumber, value);
        lines.add(lineNumber);
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        // Create an empty map for timeByLine in case it is null.
        // This could be the case if deserializing blames generated before version 0.6.0.
        if (timeByLine == null) {
            timeByLine = new HashMap<>();
        }

        return this;
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
                if (!lines.contains(otherLine)) {
                    lines.add(otherLine);
                    setInternedStringValue(commitByLine, otherLine, other.getCommit(otherLine));
                    setInternedStringValue(nameByLine, otherLine, other.getName(otherLine));
                    setInternedStringValue(emailByLine, otherLine, other.getEmail(otherLine));
                    setIntegerValue(timeByLine, otherLine, other.getTime(otherLine));
                }
            }
        }
        else {
            throw new IllegalArgumentException(
                    String.format("File names must match! This instance: %s, other instance: %s",
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

        FileBlame request = (FileBlame) o;

        if (!fileName.equals(request.fileName)) {
            return false;
        }
        if (!lines.equals(request.lines)) {
            return false;
        }
        if (!commitByLine.equals(request.commitByLine)) {
            return false;
        }
        if (!nameByLine.equals(request.nameByLine)) {
            return false;
        }
        if (!timeByLine.equals(request.timeByLine)) {
            return false;
        }
        return emailByLine.equals(request.emailByLine);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + lines.hashCode();
        result = 31 * result + commitByLine.hashCode();
        result = 31 * result + nameByLine.hashCode();
        result = 31 * result + emailByLine.hashCode();
        result = 31 * result + timeByLine.hashCode();
        return result;
    }
}
