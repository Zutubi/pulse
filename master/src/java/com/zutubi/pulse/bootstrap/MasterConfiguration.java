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

    public static final String BASE_URL = "webapp.base.url";

    public static final String AGENT_HOST = "agent.url";

    public static final String MASTER_ENABLED = "server.agent.enabled";

    //---( help configuration )---
    public static final String HELP_URL = "help.url";

    //---( queue configuration )---
    public static final String UNSATISFIABLE_RECIPE_TIMEOUT = "unsatisfiable.recipe.timeout";
    public static final long UNSATISFIABLE_RECIPE_TIMEOUT_DEFAULT = 15;

    //---( rss configuration )---

    public static final String RSS_ENABLED = "rss.enabled";

    //---( anonymous access )---

    public static final String ANONYMOUS_ACCESS_ENABLED = "anon.enabled";

    public static final String ANONYMOUS_SIGNUP_ENABLED = "signup.enabled";

    //---( scm integration )---
    public static final String SCM_POLLING_INTERVAL = "scm.polling.interval";

    //--- ( ldap integration )---

    String getAdminLogin();

    void setAdminLogin(String login);

    String getBaseUrl();

    void setBaseUrl(String host);

    String getAgentHost();

    void setAgentHost(String url);

    String getHelpUrl();

    void setHelpUrl(String helpUrl);

    Boolean getRssEnabled();

    void setRssEnabled(Boolean rssEnabled);

    Boolean getAnonymousAccessEnabled();

    void setAnonymousAccessEnabled(Boolean anonEnabled);

    Boolean getAnonymousSignupEnabled();

    void setAnonymousSignupEnabled(Boolean signupEnabled);

    Integer getScmPollingInterval();

    void setScmPollingInterval(Integer interval);

    long getUnsatisfiableRecipeTimeout();

    void setUnsatisfiableRecipeTimeout(Long timeout);

    boolean isMasterEnabled();

    void setMasterEnabled(Boolean b);
}
