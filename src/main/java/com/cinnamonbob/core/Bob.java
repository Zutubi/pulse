package com.cinnamonbob.core;

import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Bob.  The Builder.
 */
public class Bob
{
    private static final Logger LOG = Logger.getLogger(Bob.class.getName());

    private static final String CONFIG_DIR_NAME = "config";
    private static final String CONFIG_FILE_NAME = "bob.xml";
    private static final String CONFIG_EXTENSION = ".xml";
    private static final String CONFIG_ELEMENT_PROJECT_ROOT = "project-root";
    private static final String CONFIG_ELEMENT_PROJECTS = "projects";
    private static final String CONFIG_ELEMENT_PROJECT = "project";
    private static final String CONFIG_ATTR_NAME = "name";
    private static final String CONFIG_ELEMENT_SERVICES = "services";
    private static final String CONFIG_ELEMENT_SERVICE = "service";

    /**
     * The root for all of bob's internal config and output..
     */
    private File rootDir;
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
     * Factory for churning out commands based on configuration.
     */
    private CommandFactory commandFactory;
    /**
     * Factory for creating post-processors.
     */
    private PostProcessorFactory postProcessorFactory;
    /**
     * Factory for creating generic named services based on configuration.
     */
    private ServiceFactory serviceFactory;
    /**
     * Map from service name to service.
     */
    private Map<String, Service> services;
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private void loadConfig() throws ConfigException
    {
        if (!this.configDir.isDirectory())
        {
            throw new ConfigException(configDir.getPath(), "The configuration directory does not exist.");
        }

        String filename = configDir.getAbsolutePath() + File.separator + CONFIG_FILE_NAME;

        LOG.config("Loading configuration from file '" + filename + "'");
        Document      doc     = XMLConfigUtils.loadFile(filename);
        ConfigContext context = new ConfigContext(filename);
        loadElements(context, doc.getRootElement());
        LOG.config("Configuration loaded.");
    }


    private void loadElements(ConfigContext context, Element root) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, root, Arrays.asList(CONFIG_ELEMENT_PROJECT_ROOT, CONFIG_ELEMENT_PROJECTS, CONFIG_ELEMENT_SERVICES));

        for (Element current : elements)
        {
            String elementName = current.getLocalName();

            if (elementName.equals(CONFIG_ELEMENT_PROJECT_ROOT))
            {
                loadProjectRoot(context, current);
            } else if (elementName.equals(CONFIG_ELEMENT_PROJECTS))
            {
                loadProjects(context, current);
            } else if (elementName.equals(CONFIG_ELEMENT_SERVICES))
            {
                loadServices(context, current);
            } else
            {
                assert(false);
            }
        }
    }


    private void loadProjectRoot(ConfigContext context, Element element) throws ConfigException
    {
        String root = XMLConfigUtils.getElementText(context, element);

        // The project root can be either relative to bob.home or absolute.
        File rootFile = new File(root);
        if (!rootFile.isDirectory())
        {
            rootFile = new File(new File(System.getProperty("bob.home")), root);
            if (!rootFile.isDirectory())
            {
                LOG.warning("specified root '" + rootFile.getAbsolutePath() + "' is not a directory.");
                throw new ConfigException(context.getFilename(), "The specified project root '" + root + "' does not exist or is not a directory.");
            }
        }

        projectRoot = rootFile;
        LOG.config("Project root set to '" + projectRoot.getAbsolutePath() + "'.");
    }


    private void loadProjects(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(CONFIG_ELEMENT_PROJECT));

        for (Element current : elements)
        {
            loadProject(context, current);
        }
    }


    private void loadProject(ConfigContext context, Element element) throws ConfigException
    {
        String projectName = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_NAME);

        if (projects.containsKey(projectName))
        {
            throw new ConfigException(context.getFilename(), "Duplicate project name '" + projectName + "' specified.");
        }

        String projectFilename = configDir.getAbsolutePath() + File.separator + projectName + CONFIG_EXTENSION;
        File projectFile = new File(projectFilename);
        if (!projectFile.isFile())
        {
            throw new ConfigException(projectFilename, "Configuration file '" + projectFilename + "' for project '" + projectName + "' does not exist.");
        }

        LOG.config("Loading project '" + projectName + "' from file '" + projectFilename + "'");
        Project project = new Project(this, projectName, projectFilename);
        LOG.config("Project '" + projectName + "' loaded.");
        projects.put(projectName, project);
    }


    private void loadServices(ConfigContext context, Element current) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, current);

        for (Element e : elements)
        {
            Service service = serviceFactory.createService(e.getLocalName(), context, e);
            services.put(service.getServiceName(), service);
        }
    }

    /**
     * @deprecated
     */
    private void crazyBuildLoop()
    {
        for (Project project : projects.values())
        {
            LOG.info("Building project '" + project.getName() + "'");
            project.build(projectRoot);
            LOG.info("Build complete.");
        }
    }

    //=======================================================================
    // Construction
    //=======================================================================

    public Bob(String rootDir) throws ConfigException
    {
        this.rootDir = new File(rootDir);
        this.configDir = new File(this.rootDir, CONFIG_DIR_NAME);

        projects = new TreeMap<String, Project>();
        services = new TreeMap<String, Service>();

        commandFactory = new CommandFactory();
        commandFactory.registerType("executable", ExecutableCommand.class);

        postProcessorFactory = new PostProcessorFactory();
        postProcessorFactory.registerType("regex", RegexPostProcessor.class);

        serviceFactory = new ServiceFactory();
        serviceFactory.registerType("smtp", SMTPService.class);

        loadConfig();
        userManager = new UserManager(this);

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


    /**
     * @return Returns the command factory.
     */
    public CommandFactory getCommandFactory()
    {
        return commandFactory;
    }


    /**
     * @return Returns the post-processor factory.
     */
    public PostProcessorFactory getPostProcessorFactory()
    {
        return postProcessorFactory;
    }

    /**
     * @return Returns the rootDir.
     */
    public File getRootDir()
    {
        return rootDir;
    }


    public Service lookupService(String name)
    {
        return services.get(name);
    }

    //=======================================================================
    // Entry point
    //=======================================================================
    
    public static void main(String argv[])
    {
        try
        {
            Bob bob = new Bob(argv[0]);
            bob.crazyBuildLoop();
        } catch (ConfigException e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public void build(String projectName)
    {
        if (!projects.containsKey(projectName)) {
            LOG.severe("Failed to build unknown project '"+projectName+"'");
            return;
        }
        Project project = projects.get(projectName);
        project.build(projectRoot);
    }
}
