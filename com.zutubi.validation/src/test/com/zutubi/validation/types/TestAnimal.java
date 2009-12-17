package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class TestAnimal
{
    private String head;

    @Required(defaultKeySuffix = "myrequired")
    public String getHead()
    {
        return head;
    }

    public void setHead(String head)
    {
        this.head = head;
    }
}
