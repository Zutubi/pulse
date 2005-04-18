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
    private static final String BOB_HOME = "bob.home";

    private static StartupManager INSTANCE = null;
    private static final Object lock = new Object();

    private static final String CONTENT_ROOT = "content";
    private static final String PROJECT_ROOT = "work";
    private static final String CONFIG_ROOT = "config";

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

    }

    public static StartupManager getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (lock)
            {
                INSTANCE = new StartupManager();
                INSTANCE.init();
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
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public ApplicationContext getAppContext()
    {
        return context;
    }

    public String getHome()
    {
        return homeDirectory;
    }

    public String getContentRoot()
    {
        return contentDirectory;
    }

    public String getConfigRoot()
    {
        return configDirectory;
    }

    public String getProjectRoot()
    {
        return projectDirectory;
    }
}
