package com.zutubi.pulse.xwork.dispatcher;

import java.io.IOException;
import java.io.InputStream;

/**
 * <class-comment/>
 */
public interface JsonDefinitionLoader
{
    InputStream load(String location) throws IOException;
}
