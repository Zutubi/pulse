package com.zutubi.pulse.master.build.log;

/**
 * Base interface for loggers that report hook status.
 */
public interface HookLogger extends OutputLogger
{
    void hookCommenced(String name);
    void hookCompleted(String name);
}
