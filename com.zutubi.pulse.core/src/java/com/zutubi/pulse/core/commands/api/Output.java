package com.zutubi.pulse.core.commands.api;

/**
 * Basic interface for classes capable of capturing output as part of a command
 * result.  Examples of such output include built packages and HTML reports.
 */
public interface Output
{
    void capture(CommandContext context);
}
