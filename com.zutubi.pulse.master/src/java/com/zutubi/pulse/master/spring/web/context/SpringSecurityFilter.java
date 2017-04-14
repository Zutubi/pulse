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

package com.zutubi.pulse.master.spring.web.context;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import java.io.IOException;

/**
 * When placed in the web application filter chain this filter
 * provides optional security via the spring security framework.
 *
 * When enabled via {@link #enableSecurity()}, all requests will
 * be passed through the security filter chain defined by the
 * {@link #filterChainBeanName} bean. See the securityContext.xml
 * and {@link org.springframework.security.web.FilterChainProxy}
 * for more details.
 */
public class SpringSecurityFilter extends GenericFilterBean
{
    private final Object delegateMonitor = new Object();

    /**
     * Flag indicating whether or not the security filter
     * chain is enabled or not.
     */
    private boolean securityEnabled = false;

    /**
     * The name of the spring bean that defines the security
     * filter chain.
     *
     * This property must be specified.
     */
    private String filterChainBeanName;

    private Filter filterChain;

    public String getFilterChainBeanName()
    {
        return filterChainBeanName;
    }

    public void setFilterChainBeanName(String filterChainBeanName)
    {
        this.filterChainBeanName = filterChainBeanName;
    }

    /**
     * Enable filtering via the configured filter chain.
     *
     * @see #setFilterChainBeanName(String)
     */
    public void enableSecurity()
    {
        securityEnabled = true;
    }

    /**
     * Disable filtering via the configured filter chain.
     *
     * @see #setFilterChainBeanName(String)
     */
    public void disableSecurity()
    {
        securityEnabled = false;
    }

    protected void initFilterBean() throws ServletException
    {
        Assert.notNull(getFilterChainBeanName());

        initDelegate();
    }

    private void initDelegate()
    {
        synchronized (this.delegateMonitor)
        {
            if (filterChain == null)
            {
                ApplicationContext applicationContext = findApplicationContext();
                if (applicationContext != null)
                {
                    try
                    {
                        this.filterChain = applicationContext.getBean(getFilterChainBeanName(), Filter.class);
                    }
                    catch (NoSuchBeanDefinitionException e)
                    {
                        // The filter is not available yet, so load it lazily.
                    }
                }
            }
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, final FilterChain filterChain) throws IOException, ServletException
    {
        if (securityEnabled)
        {
            initDelegate();

            this.filterChain.doFilter(request, response, filterChain);
        }
        else
        {
            filterChain.doFilter(request, response);
        }
    }

    private ApplicationContext findApplicationContext()
    {
        return SpringComponentContext.getContext();
    }
}
