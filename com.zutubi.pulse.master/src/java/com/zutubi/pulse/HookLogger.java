package com.zutubi.pulse;

/**
 */
public interface HookLogger extends OutputLogger
{
    void hookCommenced(String name);
    void hookCompleted(String name);

}
