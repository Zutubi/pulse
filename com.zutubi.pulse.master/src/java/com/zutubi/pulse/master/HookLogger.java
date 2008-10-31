package com.zutubi.pulse.master;

/**
 */
public interface HookLogger extends OutputLogger
{
    void hookCommenced(String name);
    void hookCompleted(String name);

}
