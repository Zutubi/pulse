package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.StringUtils;

public class Feature extends Entity
{
    private static final int MAX_SUMMARY_LENGTH = 4095;
    private static final String SUMMARY_TRIM_MESSAGE = "... [trimmed]";

    public enum Level
    {
        INFO
                {
                    public String getPrettyString()
                    {
                        return "info";
                    }
                },
        WARNING
                {
                    public String getPrettyString()
                    {
                        return "warning";
                    }
                },
        ERROR
                {
                    public String getPrettyString()
                    {
                        return "error";
                    }
                };

        public abstract String getPrettyString();
    }

    private Level level;
    /**
     * A simple textual summary of the feature for display to users.
     */
    private String summary;

    public Feature()
    {

    }

    public Feature(Level level, String summary)
    {
        this.level = level;
        this.summary = StringUtils.trimmedString(summary, MAX_SUMMARY_LENGTH, SUMMARY_TRIM_MESSAGE);
    }

    public Level getLevel()
    {
        return level;
    }

    public String getSummary()
    {
        return summary;
    }

    private void setSummary(String summary)
    {
        this.summary = summary;
    }

    public void appendToSummary(String s)
    {
        this.summary = StringUtils.trimmedString(this.summary + s, MAX_SUMMARY_LENGTH, SUMMARY_TRIM_MESSAGE);
    }

    private String getLevelName()
    {
        return level.name();
    }

    private void setLevelName(String name)
    {
        level = Level.valueOf(name);
    }

    public boolean isPlain()
    {
        return false;
    }

    public boolean equals(Object o)
    {
        if (o instanceof Feature)
        {
            Feature other = (Feature) o;
            return other.level == level && other.summary.equals(summary);
        }

        return false;
    }

    public int hashCode()
    {
        return summary.hashCode();
    }
}
