package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public interface SystemStartupManager extends StartupManager
{
    void addCallback(StartupCallback callback);
}
