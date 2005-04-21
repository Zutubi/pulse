package com.cinnamonbob.setup;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * Handle the systems initialisation.
 *
 */
public class StartupManager
{
    /**
     * Bob Home is the systems installation directory.
     */
    private static final String BOB_HOME = "bob.home";

    private static StartupManager INSTANCE = null;
    private static final Object lock = new Object();

    /**
     * The content root relative to BOB_HOME. The content
     * root contains the web UI and related components.
     */
    private static final String CONTENT_ROOT = "content";

    /**
     * The project root relative to BOB_HOME. The project
     * root is where the systems work space.
     */
    private static final String PROJECT_ROOT = "work";

    /**
     * The config root relative to BOB_HOME. The config
     * directory contains bobs configuration files.
     */
    private static final String CONFIG_ROOT = "config";

    /**
     * The name of the spring xml configuration file.
     */
    private static final String SPRING_CONFIG = "applicationContext.xml";

    private ApplicationContext context = null;

    private String homeDirectory;
    private String contentDirectory;
    private String configDirectory;
    private String projectDirectory;

    /**
     * Private singleton constructor
     */
    private StartupManager()
    {
        init();
    }

    /**
     * Retrieve a singleton instance of the startup manager.
     * @return
     */
    public static StartupManager getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (lock)
            {
                INSTANCE = new StartupManager();
            }
        }
        return INSTANCE;
    }

    /**
     * Handle the system initialisation.
     */
    private void init()
    {

        String bobHome = System.getProperty(BOB_HOME);
        if (bobHome == null || bobHome.length() == 0)
        {
            // fatal error, BOB_HOME property needs to exist.
            throw new RuntimeException();
        }

        File homeDir = new File(bobHome);
        if (!homeDir.exists() || !homeDir.isDirectory())
        {
            // fatal error, BOB_HOME property needs to reference bobs home directory
            throw new RuntimeException();
        }

        try
        {
            homeDirectory = homeDir.getCanonicalPath();
            configDirectory = homeDirectory + File.separatorChar + CONFIG_ROOT;
            contentDirectory = homeDirectory + File.separatorChar + CONTENT_ROOT;
            projectDirectory = homeDirectory + File.separatorChar + PROJECT_ROOT;

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // initialise spring context.
        context = new ClassPathXmlApplicationContext(SPRING_CONFIG);
        context.getBean("spring-object-factory");
    }

    public ApplicationContext getAppContext()
    {
        return context;
    }

    /**
     * @see StartupManager#BOB_HOME
     */
    public String getHome()
    {
        return homeDirectory;
    }

    /**
     * @see StartupManager#CONTENT_ROOT
     */
    public String getContentRoot()
    {
        return contentDirectory;
    }

    /**
     * @see StartupManager#CONFIG_ROOT
     */
    public String getConfigRoot()
    {
        return configDirectory;
    }

    /**
     * @see StartupManager#PROJECT_ROOT
     */
    public String getProjectRoot()
    {
        return projectDirectory;
    }
}
