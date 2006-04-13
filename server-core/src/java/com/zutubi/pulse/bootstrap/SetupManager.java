/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

/**
 * <class-comment/>
 */
public interface SetupManager
{
    void prepareSetup();

    void setupComplete();

    boolean systemRequiresSetup();

    boolean systemRequiresUpgrade();
}
