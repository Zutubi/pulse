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

import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * A cut down version of the SecurityContextPersistenceFilter that retains
 * the functionality to clear the security context holder when the filter chain
 * processing is complete.
 */
public class BasicClearContextFilter implements Filter
{
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // noop.
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        try
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally
        {
            SecurityContextHolder.clearContext();
        }
    }

    public void destroy()
    {
        // noop.
    }
}
