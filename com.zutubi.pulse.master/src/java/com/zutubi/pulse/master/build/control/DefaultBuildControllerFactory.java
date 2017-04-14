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

package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The default build controller factory creates and configures a BuildController instance
 * to handle the build request.
 */
public class DefaultBuildControllerFactory implements BuildControllerFactory
{
    private ObjectFactory objectFactory;
    private MasterConfigurationManager configurationManager;

    public void init()
    {
    }

    public BuildController create(BuildRequestEvent request)
    {
        DefaultBuildController controller = objectFactory.buildBean(DefaultBuildController.class, request);
        DefaultRecipeResultCollector collector = new DefaultRecipeResultCollector(configurationManager);
        collector.setProjectConfig(request.getProjectConfig());
        controller.setCollector(collector);
        
        return controller;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
