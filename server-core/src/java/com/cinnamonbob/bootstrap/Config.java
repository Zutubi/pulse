package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public interface Config
{
    /**
     * The port on which the http server will listen for connections.
     *
     */
    int getServerPort();

    /**
     * The port on which the server will listen for admin requests.
     *
     */
    int getAdminPort();
}
