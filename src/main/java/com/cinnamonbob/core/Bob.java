package com.cinnamonbob.core;

import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Bob.  The Builder.
 */
public class Bob
{
    private static final String LOG_DIR_NAME                = "log";
    private static final String LOG_CONFIG_FILE_NAME        = "logging.properties";
    private static final String LOG_ROOT_LOGGER_NAME        = "net.anyhews.cib";
    private static final String LOG_FILE_NAME               = "bob.log";
    private static final String CONFIG_DIR_NAME             = "config";
    private static final String CONFIG_FILE_NAME            = "bob.xml";
    private static final String CONFIG_EXTENSION            = ".xml";
    private static final String CONFIG_ELEMENT_PROJECT_ROOT = "project-root";
    private static final String CONFIG_ELEMENT_PROJECTS     = "projects";
    private static final String CONFIG_ELEMENT_PROJECT      = "project";
    private static final String CONFIG_PROJECT_NAME         = "name";

    /**
     * The root for all of bob's internal config and output..
     */
    private File rootDir;
    /**
     * The directory to store log files in.
     */
    private File logDir;
    /**
     * The location of configuration files.
     */
    private File configDir;
    /**
     * The project root.  All project building output ends up in here.
     */
    private File projectRoot;
    /**
     * A mapping from project name to project.
     */
    private Map<String, Project> projects;
    /**
     * Handles the users of this system.
     */
    private UserManager userManager;
    /**
     * The top-level logger.
     */
    private Logger rootLogger;
    /**
     * Factory for churning out commands based on configuration.
     */
    private CommandFactory commandFactory;
    
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private void startupMessage(String message)
    {
        System.out.println(message);
    }


    private void reportFatalError(Exception e)
    {
        System.err.println("Fatal error prior to loading logging configuration:");
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
    }


    private boolean setupDefaultLogging()
    {
        rootLogger.setLevel(Level.CONFIG);

        try
        {
            FileHandler handler = new FileHandler(new File(logDir, LOG_FILE_NAME).getAbsolutePath());
            handler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(handler);
        }
        catch(IOException e)
        {
            // Nowhere to log this to!
            reportFatalError(e);
            return false;
        }

        return true;
    }


    private boolean loadLogging()
    {
        rootLogger = Logger.getLogger(LOG_ROOT_LOGGER_NAME);
        rootLogger.setUseParentHandlers(false);

        File            logPropertiesFile = new File(configDir, LOG_CONFIG_FILE_NAME);
        FileInputStream stream;

        try
        {
            stream = new FileInputStream(logPropertiesFile);
            startupMessage("Loading logging properties from '" + logPropertiesFile.getAbsolutePath() + "'");

            try
            {
                LogManager.getLogManager().readConfiguration(stream);
            }
            catch(IOException e)
            {
                // Busted, don't try to recover.
                reportFatalError(e);
                return false;
            }
        }
        catch(FileNotFoundException e)
        {
            // Just use the default
            startupMessage("Logging properties file '" + logPropertiesFile.getAbsolutePath() + "' not found.");
            startupMessage("Using default logging configuration.");
            return setupDefaultLogging();
        }

        return true;
    }


    private void loadConfig() throws ConfigException
    {
        if(!this.configDir.isDirectory())
        {
            throw new ConfigException(configDir.getPath(), "The configuration directory does not exist.");
        }

        String filename = configDir.getAbsolutePath() + File.separator + CONFIG_FILE_NAME;

        rootLogger.config("Loading configuration from file '" + filename + "'");
        Document doc = XMLConfigUtils.loadFile(filename);
        loadElements(filename, doc.getRootElement());
        rootLogger.config("Configuration loaded.");
    }


    private void loadElements(String filename, Element root) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, root, Arrays.asList(CONFIG_ELEMENT_PROJECT_ROOT, CONFIG_ELEMENT_PROJECTS));

        for(Element current: elements)
        {
            String  elementName = current.getLocalName();

            if(elementName.equals(CONFIG_ELEMENT_PROJECT_ROOT))
            {
                loadProjectRoot(filename, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_PROJECTS))
            {
                loadProjects(filename, current);
            }
            else
            {
                assert(false);
            }
        }
    }


    private void loadProjectRoot(String filename, Element element)  throws ConfigException
    {
        String root = XMLConfigUtils.getElementText(filename, element);
        File   rootFile = new File(root);

        if(!rootFile.isDirectory())
        {
            throw new ConfigException(filename, "The specified project root '" + root + "' does not exist or is not a directory.");
        }

        projectRoot = rootFile;
        rootLogger.config("Project root set to '" + projectRoot.getAbsolutePath() + "'.");
    }


    private void loadProjects(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_PROJECT));

        for(Element current: elements)
        {
            loadProject(filename, current);
        }
    }


    private void loadProject(String filename, Element element) throws ConfigException
    {
        String projectName = element.getAttributeValue(CONFIG_PROJECT_NAME);

        if(projectName == null)
        {
            throw new ConfigException(filename, "Project element must have '" + CONFIG_PROJECT_NAME + "' attribute.");
        }

        if(projects.containsKey(projectName))
        {
            throw new ConfigException(filename, "Duplicate project name '" + projectName + "' specified.");
        }

        String projectFilename = configDir.getAbsolutePath() + File.separator + projectName + CONFIG_EXTENSION;
        File projectFile = new File(projectFilename);
        if(!projectFile.isFile())
        {
            throw new ConfigException(projectFilename, "Configuration file '" + projectFilename + "' for project '" + projectName + "' does not exist.");
        }

        rootLogger.config("Loading project '" + projectName + "' from file '" + projectFilename + "'");
        Project project = new Project(this, projectName, projectFilename);
        rootLogger.config("Project '" + projectName + "' loaded.");
        projects.put(projectName, project);
    }


    private void crazyBuildLoop()
    {
        for(Project project: projects.values())
        {
            rootLogger.info("Building project '" + project.getName() + "'");
            project.build(projectRoot);
            rootLogger.info("Build complete.");
        }
    }

    //=======================================================================
    // Construction
    //=======================================================================

    public Bob(String rootDir) throws ConfigException
    {
        startupMessage("Bootstrapping with root '" + rootDir +"'...");

        this.rootDir   = new File(rootDir);
        this.configDir = new File(this.rootDir, CONFIG_DIR_NAME);
        this.logDir    = new File(this.rootDir, LOG_DIR_NAME);

        if(!loadLogging())
        {
            startupMessage("Logging initialisation failed.  Exiting.");
            return;
        }

        startupMessage("Bootstrap complete.  Switching to logging.");

        projects = new TreeMap<String, Project>();
        commandFactory = new CommandFactory();
        commandFactory.registerType("executable", ExecutableCommand.class);
        loadConfig();
        userManager = new UserManager(this);

        crazyBuildLoop();
    }
    
    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * @return Returns the configDir.
     */
    public File getConfigDir()
    {
        return configDir;
    }


    public boolean hasProject(String name)
    {
        return projects.containsKey(name);
    }


    public Project getProject(String name)
    {
        assert(hasProject(name));
        return projects.get(name);
    }


    /**
     * @return Returns the projectRoot.
     */
    public File getProjectRoot()
    {
        return projectRoot;
    }


    public Logger getRootLogger()
    {
        return rootLogger;
    }

    /**
     * @return Returns the command factory.
     */
    public CommandFactory getCommandFactory()
    {
        return commandFactory;
    }


    /**
     * @return Returns the rootDir.
     */
    public File getRootDir()
    {
        return rootDir;
    }
    
    //=======================================================================
    // Entry point
    //=======================================================================
    
    public static void main(String argv[])
    {
        try
        {
            Bob bob = new Bob(argv[0]);
        }
        catch(ConfigException e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}
