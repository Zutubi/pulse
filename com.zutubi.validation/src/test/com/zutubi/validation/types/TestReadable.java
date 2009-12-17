package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public interface TestReadable extends TestOpenable
{
    @Required void setContent(String content);

    String getContent();
}
