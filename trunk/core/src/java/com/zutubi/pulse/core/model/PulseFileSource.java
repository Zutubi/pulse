package com.zutubi.pulse.core.model;

import java.io.File;
import java.io.InputStream;

/**
 */
public interface PulseFileSource
{
    public InputStream getPulseFile(File baseDir);
}
