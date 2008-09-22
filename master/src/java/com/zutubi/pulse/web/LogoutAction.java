package com.zutubi.pulse.web;

import com.opensymphony.webwork.ServletActionContext;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 */
public class LogoutAction extends ActionSupport
{
    private ConfigurationManager configurationManager;
    private static final String ROOT_CONTEXT = "/";

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession();
        session.invalidate();

        // set a new acegi remember me cookie that expires immediately.
        Cookie terminate = new Cookie(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, null);
        terminate.setMaxAge(0);
        String contextPath = getContextPath();
        if (!contextPath.equals(ROOT_CONTEXT))
        {
            terminate.setPath(contextPath);
        }

        HttpServletResponse response = ServletActionContext.getResponse();
        response.addCookie(terminate);

        return SUCCESS;
    }

    protected String getContextPath()
    {
        return configurationManager.getSystemConfig().getContextPath();
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
