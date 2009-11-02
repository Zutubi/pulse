package com.zutubi.pulse.master.build.log;

/**
 */
public interface HookLogger extends OutputLogger
{
    void hookCommenced(String name);
    void hookCompleted(String name);

}
