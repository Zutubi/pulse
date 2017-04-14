/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.security.Actor;

import javax.servlet.*;
import java.io.IOException;

/**
 * This filter works with the {@link LastAccessManager} to track last access
 * times for the web ui.
 */
public class LastAccessFilter implements Filter
{
    private static final String BEAN_LAST_ACCESS_MANAGER = "lastAccessManager";
    
    private LastAccessManager lastAccessManager;

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        Actor actor = SecurityUtils.getLoggedInUser();
        if (actor != null && actor instanceof Principle)
        {
            LastAccessManager lam = getLastAccessManager();
            if (lam != null)
            {
                Principle principle = (Principle) actor;
                lastAccessManager.recordAccess(principle.getId());
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy()
    {
    }

    public LastAccessManager getLastAccessManager()
    {
        // Sadly this class defies wiring as it is initialised too early.
        if (lastAccessManager == null && SpringComponentContext.containsBean(BEAN_LAST_ACCESS_MANAGER))
        {
            lastAccessManager = SpringComponentContext.getOptionalBean(BEAN_LAST_ACCESS_MANAGER);
        }
        return lastAccessManager;
    }

    public void setLastAccessManager(LastAccessManager lastAccessManager)
    {
        this.lastAccessManager = lastAccessManager;
    }
}