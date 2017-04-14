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

package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.core.spring.SpringComponentContext;

import java.io.File;

/**
 * This manager handles the first stage of system startup. It loads the defined bootstrap spring context
 * and retrieves the required startupManager bean.
 *
 * The default bootstrapContext can be overriden by using the -Dbootstrap=<bootstrapContext> option. The
 * specified bootstrap context can be either a fully qualified file name OR a classpath reference.
 *
 */
public class SystemBootstrapManager
{
    /**
     * The default bootstrapContext file.
     */
    public static final String DEFAULT_BOOTSTRAP_CONTEXT = "com/zutubi/pulse/bootstrap/bootstrapContext.xml";

    /**
     * The bootstrap context property.
     */
    public static final String BOOTSTRAP_CONTEXT_PROPERTY = "bootstrap";

    /**
     * The name of the startup manager bean. This bean is REQUIRED in the initial context.
     */
    private static final String STARTUP_MANAGER_BEAN = "startupManager";

    /**
     * Load the systems bootstrap context.
     */
    public void loadBootstrapContext()
    {
        // lookup bootstrap context via the system properties.
        String contextName = System.getProperty(BOOTSTRAP_CONTEXT_PROPERTY, DEFAULT_BOOTSTRAP_CONTEXT);

        // lookup the context resource.
        // a) try the file system.
        // b) try the classpath.

        File contextFile = new File(contextName);
        if (contextFile.isFile())
        {
            SpringComponentContext.addFileContextDefinitions(contextName);
        }
        else // look it up on the classpath.
        {
            SpringComponentContext.addClassPathContextDefinitions(contextName);
        }
    }

    public ConfigurationManager getConfigurationManager()
    {
        return SpringComponentContext.getBean("configurationManager");
    }

    public void bootstrapSystem()
    {
        loadBootstrapContext();
        ((StartupManager) SpringComponentContext.getBean(STARTUP_MANAGER_BEAN)).init();
    }
}
