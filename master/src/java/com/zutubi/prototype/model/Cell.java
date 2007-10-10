package com.zutubi.prototype.model;

/**
 */
public class Cell
{
    private int span = 1;
    private String content;

    public Cell(String content)
    {
        this.content = content;
    }

    public Cell(int span, String content)
    {
        this.span = span;
        this.content = content;
    }

    public int getSpan()
    {
        return span;
    }

    public String getContent()
    {
        return content;
    }
}
