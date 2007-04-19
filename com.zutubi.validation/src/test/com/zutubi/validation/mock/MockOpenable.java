package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public interface MockOpenable
{
    @Required void setPages(int i);

    int getPages();
}
