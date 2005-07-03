package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public interface ApplicationPaths
{

    /**
     * The www root directory.
     *
     * @return
     */
    File getContentRoot();

    /**
     * The configuration directory.
     *
     * @return
     */
    File getConfigRoot();

    /**
     * The template root directory.
     * 
     * @return
     */
    File getTemplateRoot();

    /**
     * The user configuration directory.
     * 
     * @return
     */
    File getUserConfigRoot();
    
    /**
     * @deprecated remove.
     */
    File getApplicationRoot();
}
