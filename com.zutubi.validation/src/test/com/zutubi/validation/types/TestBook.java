package com.zutubi.validation.types;

/**
 * <class-comment/>
 */
public class TestBook implements TestReadable
{
    private String content;

    private int pages;

    public int getPages()
    {
        return pages;
    }

    public void setPages(int pages)
    {
        this.pages = pages;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }


}
