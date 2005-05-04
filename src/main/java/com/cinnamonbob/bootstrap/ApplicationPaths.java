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
     *
     * @return
     */
    File getTemplateRoot();

    /**
     * @deprecated remove.
     */
    File getApplicationRoot();

    File getUserConfigRoot();
}
