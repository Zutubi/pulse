/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 */
public class InMemoryPulseFileSource implements PulseFileSource
{
    public String data;

    public InMemoryPulseFileSource(String data)
    {
        this.data = data;
    }

    public InputStream getPulseFile(File baseDir)
    {
        return new ByteArrayInputStream(data.getBytes());
    }
}
