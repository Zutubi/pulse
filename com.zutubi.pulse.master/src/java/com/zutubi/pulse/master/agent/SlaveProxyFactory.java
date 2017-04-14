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

package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.hessian.CustomHessianProxyFactory;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.services.UncontactableSlaveService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class SlaveProxyFactory
{
    private CustomHessianProxyFactory hessianProxyFactory;

    public SlaveService createProxy(AgentConfiguration agentConfig)
    {
        return createProxy(agentConfig.getHost(), agentConfig.getPort(), agentConfig.isSsl());
    }

    public SlaveService createProxy(String host, int port, boolean ssl)
    {
        try
        {
            return unsafeCreateProxy(host, port, ssl);
        }
        catch (MalformedURLException e)
        {
            return new UncontactableSlaveService(e.getMessage());
        }
    }

    public SlaveService unsafeCreateProxy(AgentConfiguration agentConfig) throws MalformedURLException
    {
        return unsafeCreateProxy(agentConfig.getHost(), agentConfig.getPort(), agentConfig.isSsl());
    }

    private SlaveService unsafeCreateProxy(String host, int port, boolean ssl) throws MalformedURLException
    {
        URL url = new URL(ssl ? "https" : "http", host, port, "/hessian");
        return (SlaveService) hessianProxyFactory.create(SlaveService.class, url.toString());
    }

    public void setHessianProxyFactory(CustomHessianProxyFactory hessianProxyFactory)
    {
        this.hessianProxyFactory = hessianProxyFactory;
    }
}
