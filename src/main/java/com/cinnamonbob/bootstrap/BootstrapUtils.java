package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public class BootstrapUtils
{
    public static BootstrapManager getManager()
    {
        return (BootstrapManager) StartupManager.getInstance().getApplicationContext().getBean("bootstrapManager");
    }
}
