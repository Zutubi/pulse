package com.zutubi.pulse.bootstrap;

/**
 * 
 *
 */
public interface MasterApplicationConfiguration extends ApplicationConfiguration
{
    //---( server configuration )---
    public static final String ADMIN_PORT = "admin.port";

    public static final String HOST_NAME = "host.name";

    //---( help configuration )---
    public static final String HELP_URL = "help.url";

    //---( mail configuration )---

    public static final String SMTP_HOST = "mail.smtp.host";

    public static final String SMTP_FROM = "mail.smtp.from";

    public static final String SMTP_PREFIX = "mail.smtp.prefix";

    public static final String SMTP_USERNAME = "mail.smtp.username";

    public static final String SMTP_PASSWORD = "mail.smtp.password";

    //---( jabber configuration )---

    public static final String JABBER_HOST = "jabber.host";

    public static final String JABBER_PORT = "jabber.port";
    public static final int JABBER_PORT_DEFAULT = 5222;

    public static final String JABBER_USERNAME = "jabber.username";

    public static final String JABBER_PASSWORD = "jabber.password";

    public static final String JABBER_FORCE_SSL = "jabber.force.ssl";

    //---( rss configuration )---
    
    public static final String RSS_ENABLED = "rss.enabled";

    //---( anonymous access )---

    public static final String ANONYMOUS_ACCESS_ENABLED = "anon.enabled";

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

    String getJabberHost();

    void setJabberHost(String host);

    int getJabberPort();

    void setJabberPort(int port);

    String getJabberUsername();

    void setJabberUsername(String username);

    String getJabberPassword();

    void setJabberPassword(String password);

    void setLogConfig(String config);

    Boolean getJabberForceSSL();

    void setJabberForceSSL(Boolean forceSSL);

    Boolean getRssEnabled();

    void setRssEnabled(Boolean rssEnabled);

    Boolean getAnonymousAccessEnabled();

    void setAnonymousAccessEnabled(Boolean anonEnabled);

}
