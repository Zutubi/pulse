package com.zutubi.pulse.core.postprocessors.api;

/**
 */
public class Feature
{
    public enum Level
    {
        INFO,
        WARNING,
        ERROR;

        public String getPrettyString()
        {
            return name().toLowerCase();
        }
    }

    public static final long LINE_UNKNOWN = -1;

    private Level level;
    private String summary;
    private long lineNumber;
    private long firstLine;
    private long lastLine;

    public Feature(Level level, String summary)
    {
        this(level, summary, LINE_UNKNOWN);
    }

    public Feature(Level level, String summary, long lineNumber)
    {
        this(level, summary, lineNumber, lineNumber, lineNumber);
    }

    public Feature(Level level, String summary, long lineNumber, long firstLine, long lastLine)
    {
        this.level = level;
        this.summary = summary;
        this.firstLine = firstLine;
        this.lineNumber = lineNumber;
        this.lastLine = lastLine;
    }

    public Level getLevel()
    {
        return level;
    }

    public String getSummary()
    {
        return summary;
    }

    public long getLineNumber()
    {
        return lineNumber;
    }

    public long getFirstLine()
    {
        return firstLine;
    }

    public long getLastLine()
    {
        return lastLine;
    }
}
