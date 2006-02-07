package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public interface ApplicationConfiguration
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

    String getSmtpHost();

    String getHostName();

    String getSmtpFrom();
}
