package com.zutubi.pulse.core.commands.api;

/**
 * Basic interface for classes capable of capturing artifacts as part of a command
 * result.  Examples of such artifacts include built packages and HTML reports.
 */
public interface Artifact
{
    /**
     * Called to capture the output, after a command has executed.  To capture
     * the output, register it against the given context.
     *
     * @param context context in which the associated command executed, used to
     *                register captured outputs
     */
    void capture(CommandContext context);
}
