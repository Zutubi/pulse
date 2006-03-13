package com.cinnamonbob.core.model;

public class Feature extends Entity
{
    public enum Level
    {
        ERROR
                {
                    public String getPrettyString()
                    {
                        return "error";
                    }

                    public boolean isGreaterThan(Level level)
                    {
                        return level != ERROR;
                    }
                },
        INFO
                {
                    public String getPrettyString()
                    {
                        return "info";
                    }

                    public boolean isGreaterThan(Level level)
                    {
                        return false;
                    }
                },
        WARNING
                {
                    public String getPrettyString()
                    {
                        return "warning";
                    }

                    public boolean isGreaterThan(Level level)
                    {
                        return level == INFO;
                    }
                };


        public abstract boolean isGreaterThan(Level level);
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
        this.summary = summary;
    }

    public Level getLevel()
    {
        return level;
    }

    public String getSummary()
    {
        return summary;
    }

    private String getLevelName()
    {
        return level.name();
    }

    private void setLevelName(String name)
    {
        level = Level.valueOf(name);
    }

    private void setSummary(String summary)
    {
        this.summary = summary;
    }
}
