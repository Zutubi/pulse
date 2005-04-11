package com.cinnamonbob.core;

public class Feature
{
    /**
     * The category is a flexible grouping facility for features.  A category
     * implies a level.  Built-in categories include "Error", "Warning" and
     *  "Info", which map in the obvious way to levels.
     */
    private String category;
    /**
     * A simple textual summary of the feature for display to users.
     */
    private String summary;


    public Feature(String category, String summary)
    {
        this.category = category;
        this.summary = summary;
    }


    public String getCategory()
    {
        return category;
    }
    

    public String getSummary()
    {
        return summary;
    }
    
}
