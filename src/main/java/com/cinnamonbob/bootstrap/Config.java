package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public interface Config
{
    int getServerPort();
    int getAdminPort();
    File getProjectRoot();
}
