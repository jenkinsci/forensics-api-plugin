package io.jenkins.plugins.forensics.blame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.errorprone.annotations.FormatMethod;

import io.jenkins.plugins.forensics.util.FilteredLog;

/**
 * Provides access to the blame information for a collection of workspace files. File names must use absolute paths.
 * Additionally, info and error messages during the SCM processing can be stored.
 *
 * @author Ullrich Hafner
 */
public class Blames implements java.io.Serializable {
    private static final long serialVersionUID = -1192940891942480612L;

    private final Map<String, FileBlame> blamesPerFile = new HashMap<>();
    private transient FilteredLog log = createLog();

    /**
     * Adds the specified blame to this collection of blames.
     *
     * @param additionalBlame
     *         the blame to add
     */
    public void add(final FileBlame additionalBlame) {
        merge(additionalBlame.getFileName(), additionalBlame);
    }

    /**
     * Merges all specified blames with the current collection of blames.
     *
     * @param other
     *         the blames to add
     */
    public void addAll(final Blames other) {
        for (String otherFile : other.getFiles()) {
            FileBlame otherRequest = other.getBlame(otherFile);
            merge(otherFile, otherRequest);
        }
    }

    private void merge(final String otherFile, final FileBlame otherRequest) {
        if (contains(otherFile)) {
            getBlame(otherFile).merge(otherRequest);
        }
        else {
            blamesPerFile.put(otherFile, otherRequest);
        }
    }

    /**
     * Returns whether there are files with blames.
     *
     * @return {@code true} if there a no blames available, {@code false} otherwise
     */
    public boolean isEmpty() {
        return blamesPerFile.isEmpty();
    }

    /**
     * Returns the number of files with blames.
     *
     * @return number of affected files with blames
     */
    public int size() {
        return blamesPerFile.keySet().size();
    }

    /**
     * Returns whether there are blames for the specified file.
     *
     * @param fileName
     *         the relative or absolute path of the file
     *
     * @return {@code true} if the file already has been added, {@code false} otherwise
     */
    public boolean contains(final String fileName) {
        return blamesPerFile.containsKey(fileName);
    }

    /**
     * Returns all files with blames.
     *
     * @return the files with blames
     */
    public Set<String> getFiles() {
        return new HashSet<>(blamesPerFile.keySet());
    }

    /**
     * Returns the blame information for the specified file.
     *
     * @param fileName
     *         the absolute path of the file
     *
     * @return the blame information for the specified file.
     * @throws NoSuchElementException
     *         if the file name is not registered
     */
    public FileBlame getBlame(final String fileName) {
        if (blamesPerFile.containsKey(fileName)) {
            return blamesPerFile.get(fileName);
        }
        throw new NoSuchElementException(String.format("No blame information for file '%s' stored", fileName));
    }

    /**
     * Logs the specified information message. Use this method to log any useful information when composing this log.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     */
    @FormatMethod
    public void logInfo(final String format, final Object... args) {
        log.logInfo(format, args);
    }

    /**
     * Logs the specified error message. Use this method to log any error when composing this log.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     */
    @FormatMethod
    public void logError(final String format, final Object... args) {
        log.logError(format, args);
    }

    /**
     * Logs the specified exception. Use this method to log any exception when composing this log.
     *
     * @param exception
     *         the exception to log
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     */
    @FormatMethod
    public void logException(final Exception exception, final String format, final Object... args) {
        log.logException(exception, format, args);
    }

    /**
     * Writes a summary message to the reports' error log that denotes the total number of errors that have been
     * reported.
     */
    public void logSummary() {
        log.logSummary();
    }

    public List<String> getErrorMessages() {
        return log.getErrorMessages();
    }

    public List<String> getInfoMessages() {
        return log.getInfoMessages();
    }

    /**
     * Called after de-serialization to retain backward compatibility and to restore transient fields.
     *
     * @return this
     */
    protected Object readResolve() {
        if (log == null) {
            log = createLog();
        }
        return this;
    }

    private FilteredLog createLog() {
        return new FilteredLog("Errors while extracting author and commit information from Git:");
    }
}
