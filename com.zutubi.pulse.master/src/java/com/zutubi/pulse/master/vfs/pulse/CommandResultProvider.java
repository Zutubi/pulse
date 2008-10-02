package com.zutubi.pulse.master.vfs.pulse;

import com.zutubi.pulse.core.model.CommandResult;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a command result instance.
 *
 * @see com.zutubi.pulse.core.model.CommandResult
 */
public interface CommandResultProvider
{
    CommandResult getCommandResult() throws FileSystemException;

    long getCommandResultId() throws FileSystemException;
}