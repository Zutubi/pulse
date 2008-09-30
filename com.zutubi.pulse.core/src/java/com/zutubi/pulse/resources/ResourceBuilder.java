package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;

import java.io.File;

/**
 */
public interface ResourceBuilder
{
    static final String PROPERTY_SEPARATOR                = ".";
    static final String PROPERTY_SUFFIX_BINARY            = "bin";
    static final String PROPERTY_SUFFIX_DIRECTORY         = "dir";
    static final String PROPERTY_SUFFIX_LIBRARY           = "lib";
    static final String PROPERTY_SUFFIX_BINARY_DIRECTORY  = PROPERTY_SUFFIX_BINARY + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_DIRECTORY;
    static final String PROPERTY_SUFFIX_LIBRARY_DIRECTORY = PROPERTY_SUFFIX_LIBRARY + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_DIRECTORY;

    Resource buildResource(File file);
}
