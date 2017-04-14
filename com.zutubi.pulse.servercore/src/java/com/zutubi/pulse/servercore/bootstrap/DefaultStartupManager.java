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

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.config.ConfigSupport;
import com.zutubi.util.config.FileConfig;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class DefaultStartupManager implements StartupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultStartupManager.class);

    private ConfigurationManager configurationManager;
    private ObjectFactory objectFactory;

    /**
     * Static value indicating that the startup manager has not started yet.
     */
    private static final long NOT_STARTED = -1;

    private long startTime = NOT_STARTED;

    private boolean starting = false;

    private List<String> startupTasks = Collections.emptyList();
    private List<String> postStartupTasks = Collections.emptyList();


    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("The system has already started.");
        }
        if (isSystemStarting())
        {
            throw new StartupException("The system is currently starting up.");
        }

        checkForGCJ();
        
        try
        {
            starting = true;

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                public void uncaughtException(Thread t, Throwable e)
                {
                    java.util.logging.Logger.getLogger("").log(Level.SEVERE, "Uncaught exception: " + e.getMessage(), e);
                }
            });

            // record the startup config to the config directory.
            writeSystemRuntimePropertiesToFile();

            // application contexts have finished loading
            // i) run startup tasks
            runStartupTasks(startupTasks);

            // record the start time
            startTime = System.currentTimeMillis();

            // send a message to the rest of the system that all is good to go.
            EventManager eventManager = SpringComponentContext.getBean("eventManager");
            eventManager.publish(new SystemStartedEvent(this));

            runStartupTasks(postStartupTasks);
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    private void checkForGCJ()
    {
        String vm = System.getProperty("java.vm.name");
        if(vm != null && vm.toLowerCase().contains("gcj"))
        {
            System.err.println("You appear to be running the GNU Classpath JVM (libgcj/gij).  Due to missing\n" +
                    "features in this VM, Pulse does currently not support it.  Please consider\n" +
                    "installing another JVM (a free one is provided by Oracle for Linux systems).");
            System.exit(1);
        }
    }

    private void writeSystemRuntimePropertiesToFile()
    {
        SystemConfiguration config = configurationManager.getSystemConfig();
        File configRoot = configurationManager.getSystemPaths().getConfigRoot();
        File startupConfigFile = new File(configRoot, "runtime.properties");
        ConfigSupport startupConfig = new ConfigSupport(new FileConfig(startupConfigFile));
        startupConfig.setProperty(SystemConfiguration.CONTEXT_PATH, config.getContextPath());
        startupConfig.setInteger(SystemConfiguration.WEBAPP_PORT, config.getServerPort());
    }

    private void runStartupTasks(List<String> startupRunnables)
    {
        for (String name : startupRunnables)
        {
            StartupTask instance = null;

            try
            {
                instance = objectFactory.buildBean(name, StartupTask.class);
                instance.execute();
            }
            catch (Exception e)
            {
                boolean halt = true;

                if(instance == null)
                {
                    LOG.severe("Unable to create startup task '" + name + "': " + e.getMessage(), e);
                }
                else
                {
                    halt = instance.haltOnFailure();
                    LOG.log(halt ? Level.SEVERE : Level.WARNING, "Failed to run startup task " + name + ". Reason: " + e.getMessage(), e);
                }

                if(halt)
                {
                    throw new StartupException(e);
                }
            }
        }
    }

    /**
     * Indicates whether or not the system is currently starting up.
     *
     * @return true if the system is starting, false otherwise.
     */
    public boolean isSystemStarting()
    {
        return starting;
    }

    /**
     * Indicates whether or not the system has started.
     *
     * @return true if the system has started, false otherwise.
     */
    public boolean isSystemStarted()
    {
        return startTime != NOT_STARTED;
    }

    /**
     * Returns the amount time (in milliseconds) since the system started.
     *
     * @return the uptime in milliseconds, or 0 if the system has not yet started.
     */
    public long getUptime()
    {
        if (isSystemStarted())
        {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    /**
     * Returns the system start time in milliseconds, or -1 if the system has not
     * yet started.
     *
     * @return start time in milliseconds.
     */
    public long getStartTime()
    {
        return startTime;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setStartupTasks(List<String> startupTasks)
    {
        this.startupTasks = startupTasks;
    }

    public void setPostStartupTasks(List<String> postStartupTasks)
    {
        this.postStartupTasks = postStartupTasks;
    }
}
