package com.zutubi.pulse.core.postprocessors.api;

/**
 * A feature is a useful piece of information attached to a result.  Features
 * are located by post-processors as they process build artifacts.  A typical
 * example of a feature is a compiler error messages.
 * <p/>
 * Note that this class is designed to be immutable.
 */
public class Feature
{
    /**
     * Available feature severities, indicating what type of feature has been
     * found.
     */
    public enum Level
    {
        /**
         * The feature is informative, and has no reflection on the build
         * result.
         */
        INFO,
        /**
         * The feature is a warning that something is likely to be wrong (by
         * default this will not fail the build, but this is configurable).
         */
        WARNING,
        /**
         * The feature highlights a detected error (by default this would fail
         * the build, but this is configurable).
         */
        ERROR;

        /**
         * @return a human-readable version of the level
         */
        public String getPrettyString()
        {
            return name().toLowerCase();
        }
    }

    /**
     * Value used when the feature does not correspond to a particular line in
     * an artifact file, or the line cannot be determined.
     */
    public static final long LINE_UNKNOWN = -1;

    private final Level level;
    private final String summary;
    private final long lineNumber;
    private final long firstLine;
    private final long lastLine;

    /**
     * Creates a feature not tied to any specific line number in a file.
     *
     * @param level   the severity of the feature
     * @param summary a textual summary of the feature for presentation to the
     *                user (e.g. a compiler error message)
     */
    public Feature(Level level, String summary)
    {
        this(level, summary, LINE_UNKNOWN);
    }

    /**
     * Creates a feature found at a specific line in an artifact file, with no
     * leading or trailing context lines.
     *
     * @param level      the severity of the feature
     * @param summary    a textual summary of the feature for presentation to
     *                   the user (e.g. a compiler error message)
     * @param lineNumber one-based number of the line at which the feature was
     *                   found
     */
    public Feature(Level level, String summary, long lineNumber)
    {
        this(level, summary, lineNumber, lineNumber, lineNumber);
    }

    /**
     * Creates a feature found at a specific line in an artifact file, with the
     * specified leading and trailing context lines.
     *
     * @param level      the severity of the feature
     * @param summary    a textual summary of the feature for presentation to
     *                   the user (e.g. a compiler error message)
     * @param lineNumber one-based number of the line at which the feature was
     *                   found
     * @param firstLine  one-based number of the first line covered by the
     *                   leading context of this feature
     * @param lastLine   one-based number of the last line covered by the
     *                   trailing context of this feature
     */
    public Feature(Level level, String summary, long lineNumber, long firstLine, long lastLine)
    {
        this.level = level;
        this.summary = summary;
        this.firstLine = firstLine;
        this.lineNumber = lineNumber;
        this.lastLine = lastLine;
    }

    /**
     * @return this feature's severity
     */
    public Level getLevel()
    {
        return level;
    }

    /**
     * @return a textual summary of the feature for presentation to the user
     *        (when the feature is found in a file, usually the lines
     *         corresponding to the feature and its context)
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * @return the one-based line number at which this feature was found in the
     *         corresponding artifact file, may be {@link #LINE_UNKNOWN}
     */
    public long getLineNumber()
    {
        return lineNumber;
    }

    /**
     * @return the one-based line number of the first line covered by the
     *         leading context of this feature, may be {@link #LINE_UNKNOWN}
     */
    public long getFirstLine()
    {
        return firstLine;
    }

    /**
     * @return the one-based line number of the last line covered by the
     *         trailing context of this feature, may be {@link #LINE_UNKNOWN}
     */
    public long getLastLine()
    {
        return lastLine;
    }
}
