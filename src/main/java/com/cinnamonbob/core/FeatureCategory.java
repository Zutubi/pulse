package com.cinnamonbob.core;

/**
 * Contains information about a single feature category.
 * 
 * @author jsankey
 */
public class FeatureCategory
{
    /**
     * The level or severity of the category.
     */
    public enum Level
    {
        /**
         * The feature is an error or problem.  Something actually went
         * wrong.
         */
        ERROR,
        /**
         * The feature is a warning that maybe something went wrong.
         */
        WARNING,
        /**
         * The feature is just an interesting piece of information.
         */
        INFO
    }

    /**
     * The name of the category.  Must be unique in a server.
     */
    private String name;
    /**
     * The category level or severity.
     */
    private Level level;
    /**
     * A descriptive title for the category.  Defaults to the name if not
     * specified.
     */
    private String title;
    
    
    public FeatureCategory(String name, Level level, String title)
    {
        this.name = name;
        this.level = level;
        
        if(title == null)
        {
            this.title = name;
        }
        else
        {
            this.title = title;
        }
    }


    public Level getLevel()
    {
        return level;
    }


    public String getName()
    {
        return name;
    }


    public String getTitle()
    {
        return title;
    }
}
