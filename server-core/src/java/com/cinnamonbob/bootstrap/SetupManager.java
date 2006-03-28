package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public interface SetupManager
{
    void executePostBobHomeSetup();

    void setupComplete() throws Exception;
}
