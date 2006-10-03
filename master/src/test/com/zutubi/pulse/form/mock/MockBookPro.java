package com.zutubi.pulse.form.mock;

import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.form.descriptor.annotation.Field;

/**
 * Similar to a mock book, except that this time it has an annotation defined field.
 */
public class MockBookPro
{
    private String title;
    private String author;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Required @Field(fieldType = "author") public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }
}
