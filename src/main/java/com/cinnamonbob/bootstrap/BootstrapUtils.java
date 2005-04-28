package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public class BootstrapUtils
{
    private static final String BEAN_NAME = "bootstrapManager";

    public static BootstrapManager getManager()
    {
        return (BootstrapManager) StartupManager.getBean(BEAN_NAME);
    }
}
