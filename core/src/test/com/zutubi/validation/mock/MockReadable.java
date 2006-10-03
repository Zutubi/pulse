package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public interface MockReadable extends MockOpenable
{
    @Required void setContent(String content);

    String getContent();
}
