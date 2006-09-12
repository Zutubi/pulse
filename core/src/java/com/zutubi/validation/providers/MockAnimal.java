package com.zutubi.validation.providers;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class MockAnimal
{
    private String head;

    @Required
    public String getHead()
    {
        return head;
    }

    public void setHead(String head)
    {
        this.head = head;
    }
}
