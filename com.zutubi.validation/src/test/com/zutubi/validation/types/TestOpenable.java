package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public interface TestOpenable
{
    @Required void setPages(int i);

    int getPages();
}
