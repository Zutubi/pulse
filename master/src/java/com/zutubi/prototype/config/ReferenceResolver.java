package com.zutubi.prototype.config;

import com.zutubi.prototype.type.Instantiator;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.pulse.core.config.Configuration;

/**
 * A basic interface for reference resolution.
 *
 * @see com.zutubi.prototype.config.ConfigurationReferenceManager
 */
public interface ReferenceResolver
{
    Configuration resolveReference(String fromPath, long toHandle, Instantiator instantiator) throws TypeException;
}
