package com.zutubi.validation.mock;

/**
 * <class-comment/>
 */
public class MockBook implements MockReadable
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
