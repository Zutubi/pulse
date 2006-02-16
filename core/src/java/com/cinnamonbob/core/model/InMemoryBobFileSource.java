package com.cinnamonbob.core.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 */
public class InMemoryBobFileSource implements BobFileSource
{
    public String data;

    public InMemoryBobFileSource(String data)
    {
        this.data = data;
    }

    public InputStream getBobFile(File baseDir)
    {
        return new ByteArrayInputStream(data.getBytes());
    }
}
