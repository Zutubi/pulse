package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public interface ResourceConstructor
{
    boolean isResourceHome(String home);

    boolean isResourceHome(File antHome);

    Resource createResource(String home) throws IOException;

    Resource createResource(File home) throws IOException;
}
