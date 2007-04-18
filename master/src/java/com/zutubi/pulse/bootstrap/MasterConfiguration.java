package com.zutubi.pulse.bootstrap;

/**
 * 
 *
 */
public interface MasterConfiguration
{
    //---( server configuration )---
    public static final String ADMIN_LOGIN = "admin.login";

    public static final String MASTER_ENABLED = "server.agent.enabled";

    String getAdminLogin();

    void setAdminLogin(String login);

    boolean isMasterEnabled();

    void setMasterEnabled(Boolean b);
}
