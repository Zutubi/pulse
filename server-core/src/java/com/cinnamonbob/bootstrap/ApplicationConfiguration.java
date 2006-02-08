package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public interface ApplicationConfiguration
{
    //---( server configuration )---
    public static final String ADMIN_PORT = "admin.port";

    public static final String WEBAPP_PORT = "webapp.port";

    public static final String HOST_NAME = "host.name";

    //---( mail configuration )---

    public static final String SMTP_HOST = "mail.smtp.host";

    public static final String SMTP_FROM = "mail.smtp.from";

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
