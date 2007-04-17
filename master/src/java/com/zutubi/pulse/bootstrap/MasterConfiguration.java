package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.logging.LogConfiguration;

/**
 * 
 *
 */
public interface MasterConfiguration extends LogConfiguration
{
    //---( server configuration )---
    public static final String ADMIN_LOGIN = "admin.login";

    public static final String AGENT_HOST = "agent.url";

    public static final String MASTER_ENABLED = "server.agent.enabled";

    String getAdminLogin();

    void setAdminLogin(String login);

    String getAgentHost();

    void setAgentHost(String url);

    boolean isMasterEnabled();

    void setMasterEnabled(Boolean b);
}
