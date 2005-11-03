package com.cinnamonbob.core.model;

public class PlainFeature extends Feature
{
    /**
     * One-based line number of the line that contains this feature.
     */
    private long lineNumber;


    public PlainFeature()
    {

    }

    public PlainFeature(Level level, String summary, long lineNumber)
    {
        super(level, summary);

        this.lineNumber = lineNumber;
    }


    public long getLineNumber()
    {
        return lineNumber;
    }

    private void setLineNumber(long line)
    {
        lineNumber = line;
    }
}