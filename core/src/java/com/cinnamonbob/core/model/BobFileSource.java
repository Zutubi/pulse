package com.cinnamonbob.core.model;

import java.io.File;
import java.io.InputStream;

/**
 */
public interface BobFileSource
{
    public InputStream getBobFile(File workDir);
}
