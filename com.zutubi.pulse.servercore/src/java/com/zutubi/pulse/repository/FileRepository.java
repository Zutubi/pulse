package com.zutubi.pulse.repository;

import com.zutubi.pulse.core.PulseException;

import java.io.File;

/**
 */
public interface FileRepository
{
    File getPatchFile(long userId, long number) throws PulseException;
}
