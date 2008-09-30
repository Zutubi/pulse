package com.zutubi.pulse.bootstrap;

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
