/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web;

import com.opensymphony.webwork.ServletActionContext;
import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <class-comment/>
 */
public class LogoutAction extends ActionSupport
{
    private static final String ROOT_CONTEXT = "/";

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession();
        session.invalidate();

        // set a new acegi remember me cookie that expires immediately.
        Cookie terminate = new Cookie(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, null);
        terminate.setMaxAge(0);
        terminate.setPath(ROOT_CONTEXT);

        HttpServletResponse response = ServletActionContext.getResponse();
        response.addCookie(terminate);

        return SUCCESS;
    }
}
