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

    //---( help configuration )---
    public static final String HELP_URL = "help.url";

    //---( mail configuration )---

    public static final String SMTP_HOST = "mail.smtp.host";

    public static final String SMTP_FROM = "mail.smtp.from";

    public static final String SMTP_PREFIX = "mail.smtp.prefix";

    public static final String SMTP_USERNAME = "mail.smtp.username";

    public static final String SMTP_PASSWORD = "mail.smtp.password";

    //---( logging configuration )---

    public static final String LOGGING_CONFIG = "log.config";

    /**
     * The port on which the http server will listen for connections.
     */
    int getServerPort();

    /**
     * The port on which the server will listen for admin requests.
     */
    int getAdminPort();

    String getHostName();

    void setHostName(String host);

    String getHelpUrl();

    void setHelpUrl(String helpUrl);

    String getSmtpHost();

    void setSmtpHost(String host);

    String getSmtpFrom();

    void setSmtpFrom(String from);

    String getSmtpPrefix();

    void setSmtpPrefix(String prefix);

    String getSmtpUsername();

    void setSmtpUsername(String username);

    String getSmtpPassword();

    void setSmtpPassword(String password);

    String getLogConfig();

    void setLogConfig(String config);
}
