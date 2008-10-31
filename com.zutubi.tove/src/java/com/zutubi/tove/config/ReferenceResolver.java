package com.zutubi.tove.config;

import com.zutubi.tove.type.Instantiator;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.config.api.Configuration;

/**
 * A basic interface for reference resolution.
 *
 * @see com.zutubi.tove.config.ConfigurationReferenceManager
 */
public interface ReferenceResolver
{
    Configuration resolveReference(String fromPath, long toHandle, Instantiator instantiator) throws TypeException;
}
