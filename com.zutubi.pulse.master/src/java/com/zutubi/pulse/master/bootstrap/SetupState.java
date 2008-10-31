package com.zutubi.pulse.master.bootstrap;

/**
 * States reached through the setup process.
 */
public enum SetupState
{
    DATA,
    DATABASE,
    MIGRATE,
    RESTORE,
    LICENSE,
    UPGRADE,
    SETUP,
    STARTING,
}
