package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.CommandResult;

/**
 * A provider interface that indicates the current node represents a command result instance.
 *
 * @see com.zutubi.pulse.core.model.CommandResult
 */
public interface CommandResultProvider
{
    CommandResult getCommandResult();

    long getCommandResultId();
}