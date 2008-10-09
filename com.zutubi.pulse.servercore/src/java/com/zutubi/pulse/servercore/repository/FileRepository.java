package com.zutubi.pulse.servercore.repository;

import com.zutubi.pulse.core.api.PulseException;

import java.io.File;

/**
 */
public interface FileRepository
{
    File getPatchFile(long userId, long number) throws PulseException;
}
