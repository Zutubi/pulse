package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.api.PulseException;

import java.io.InputStream;

/**
 * A testing resolver that does not support resolution.
 */
public class ImportingNotSupportedFileResolver implements FileResolver
{
    public InputStream resolve(String path) throws Exception
    {
        throw new PulseException("Importing not supported");
    }
}
