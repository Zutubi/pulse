package com.zutubi.tove.config.api;

import com.zutubi.tove.annotations.ID;

/**
 * Simple interface for the common case of named configuration objects.
 */
public interface NamedConfiguration extends Configuration
{
    @ID
    String getName();
    void setName(String name);
}
