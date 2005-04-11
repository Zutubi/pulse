package com.cinnamonbob.core;

public class PlainFeature extends Feature
{
    /**
     * One-based line number of the line that contains this feature.
     */
    private long lineNumber;

    
    public PlainFeature(String category, String summary, long lineNumber)
    {
        super(category, summary);
        
        this.lineNumber = lineNumber;
    }


    public long getLineNumber()
    {
        return lineNumber;
    }
}
