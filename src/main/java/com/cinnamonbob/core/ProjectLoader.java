package com.cinnamonbob.core;

import com.cinnamonbob.util.Pair;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A temporary project loader using the existing implementation. The
 * idea is to remove all of the config specific code into this loader
 * instance to easy the conversion to the new config loading system.
 */
public class ProjectLoader
{
    private static final String CONFIG_ELEMENT_DESCRIPTION = "description";
    private static final String CONFIG_ELEMENT_POST_PROCESSOR = "post-processor";
    private static final String CONFIG_ELEMENT_RECIPE = "recipe";
    private static final String CONFIG_ELEMENT_SCHEDULE = "schedule";
    private static final String VARIABLE_WORK_DIR = "work.dir";

    private static final String COMMAND_CONFIG_ATTR_NAME = "name";
    private static final String COMMAND_CONFIG_ATTR_FORCE = "force";
    private static final String COMMAND_CONFIG_ELEMENT_ARTIFACT = "artifact";
    private static final String COMMAND_CONFIG_ELEMENT_PROCESS = "process";

    private static final String ARTIFACT_CONFIG_ATTR_NAME = "name";
    private static final String ARTIFACT_CONFIG_ATTR_TITLE = "title";
    private static final String ARTIFACT_CONFIG_ATTR_TYPE = "type";
    private static final String ARTIFACT_CONFIG_ATTR_FROM_FILE = "from-file";
    private static final String ARTIFACT_CONFIG_ATTR_TO_FILE = "to-file";

    private static final String PROCESS_CONFIG_ATTR_PROCESSOR = "processor";
    private static final String PROCESS_CONFIG_ATTR_ARTIFACT  = "artifact";
    
    private static final String EXE_CONFIG_ATTR_EXECUTABLE        = "exe";
    private static final String EXE_CONFIG_ATTR_ARGUMENTS         = "args";
    private static final String EXE_CONFIG_ATTR_WORKING_DIRECTORY = "working-dir";
    private static final String EXE_CONFIG_ELEMENT_ENVIRONMENT    = "environment";
    private static final String EXE_CONFIG_ATTR_NAME              = "name";
    private static final String EXE_CONFIG_ATTR_VALUE             = "value";

    private static final String PP_CONFIG_ATTR_NAME = "name";

    private static final String REGEX_CONFIG_ELEMENT_PATTERN = "pattern";
    private static final String REGEX_CONFIG_ATTR_CATEGORY   = "category";
    private static final String REGEX_CONFIG_ATTR_EXPRESSION = "expression";
       
    private static final String P4_CONFIG_ATTR_PORT       = "port";
    private static final String P4_CONFIG_ATTR_USER       = "user";
    private static final String P4_CONFIG_ATTR_PASSWORD   = "password";
    private static final String P4_CONFIG_ATTR_CLIENT     = "client";
    private static final String P4_CONFIG_ATTR_PATH       = "path";
    private static final String P4_VARIABLE_PORT          = "p4.port";
    private static final String P4_VARIABLE_USER          = "p4.user";
    private static final String P4_VARIABLE_PASSWORD      = "p4.password";
    private static final String P4_VARIABLE_CLIENT        = "p4.client";

    private static final String SVN_CONFIG_ATTR_USER       = "user";
    private static final String SVN_CONFIG_ATTR_PASSWORD   = "password";
    private static final String SVN_CONFIG_ATTR_KEY_FILE   = "key-file";
    private static final String SVN_CONFIG_ATTR_PASSPHRASE = "passphrase";
    private static final String SVN_CONFIG_ATTR_URL        = "url";
    private static final String SVN_CONFIG_ATTR_PATH       = "path";
    private static final String SVN_VARIABLE_USER          = "svn.user";
    private static final String SVN_VARIABLE_PASSWORD      = "svn.password";
    private static final String SVN_VARIABLE_KEY_FILE      = "svn.keyfile";
    private static final String SVN_VARIABLE_PASSPHRASE    = "svn.passphrase";
    private static final String SVN_VARIABLE_URL           = "svn.url";

    public Project loadProject(String name, String filename) throws ConfigException
    {
        Project project = new Project();
        project.setName(name);
        project.setNextBuildId(getBuildManager().determineNextAvailableBuildId(project));
        
        loadConfig(project, filename);

        return project;
    }

    private void loadDescription(Project project, ConfigContext context, Element element) throws ConfigException
    {
        project.setDescription(XMLConfigUtils.getElementText(context, element));
    }


    private void loadPostProcessor(Project project, ConfigContext context, Element element) throws ConfigException
    {
        PostProcessorCommon post = new PostProcessorCommon();
        post.setName(XMLConfigUtils.getAttributeValue(context, element, PP_CONFIG_ATTR_NAME));

        List<Element> childElements = XMLConfigUtils.getElements(context, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(context.getFilename(), "Post processor '" + post.getName() + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        PostProcessor pp = null;
        String localName = childElements.get(0).getLocalName();
        if (localName.equals("regex"))
        {
            pp = loadRegexPP(context, childElements.get(0), post, project);
        }
        post.setPostProcessor(pp);

        if (project.postProcessors.containsKey(post.getName()))
        {
            throw new ConfigException(context.getFilename(), "Project '" + project.getName() + "' already contains a post-processor named '" + post.getName() + "'");
        }

        project.postProcessors.put(post.getName(), post);
    }

    private PostProcessor loadRegexPP(ConfigContext context, Element e, PostProcessorCommon common, Project project) throws ConfigException
    {
        RegexPostProcessor pp = new RegexPostProcessor();
        pp.setPostProcessorCommon(common);
        List<Element> elements = XMLConfigUtils.getElements(context, e, Arrays.asList(REGEX_CONFIG_ELEMENT_PATTERN));
        
        for(Element current: elements)
        {
            loadPattern(pp, context, current, project);
        }                
        return pp;
    }
    
    private void loadPattern(RegexPostProcessor regex, ConfigContext context, Element element, Project project) throws ConfigException
    {
        String category   = XMLConfigUtils.getAttributeValue(context, element, REGEX_CONFIG_ATTR_CATEGORY);
        String expression = XMLConfigUtils.getAttributeValue(context, element, REGEX_CONFIG_ATTR_EXPRESSION);
        
        if(!project.getCategoryRegistry().hasCategory(category))
        {
            throw new ConfigException(context.getFilename(), "Post processor '" + regex.common.getName() + "' refers to unknown category '" + category +"'");
        }
        
        try
        {
            Pattern pattern = Pattern.compile(expression);
            regex.patterns.add(new Pair<String, Pattern>(category, pattern));
        }
        catch(PatternSyntaxException e)
        {
            throw new ConfigException(context.getFilename(), "Post processor '" + regex.common.getName() + "' contains invalid expression: " + e.getMessage());
        }
        
    }
    
    private void loadRecipe(Project project, ConfigContext context, Element element) throws ConfigException
    {
        Recipe recipe = new Recipe();
        loadCommands(project, recipe, context, element);
        project.recipes.add(recipe);
    }

    private void loadCommands(Project project, Recipe recipe, ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList("command"));

        for (Element current : elements)
        {
            CommandCommon command = loadCommand(project, context, current);
            recipe.addCommand(command);
        }
    }

    private CommandCommon loadCommand(Project project, ConfigContext context, Element element) throws ConfigException
    {

        CommandCommon commandCommon = new CommandCommon();

        commandCommon.setName(XMLConfigUtils.getAttributeValue(context, element, COMMAND_CONFIG_ATTR_NAME));

        if (element.getAttributeValue(COMMAND_CONFIG_ATTR_FORCE) == null)
            commandCommon.setForce(false);
        else
            commandCommon.setForce(true);

        List<Element> childElements = XMLConfigUtils.getElements(context, element);

        if (childElements.size() == 0)
        {
            throw new ConfigException(context.getFilename(), "Command '" + commandCommon.getName() + "' contains no child elements.");
        }

        // The first child is the specific command element
        Command command = null;
        String elementName = childElements.get(0).getLocalName();
        if (elementName.equals("executable"))
        {
            command = loadExeConfig(commandCommon, context, childElements.get(0));
        }
        else if (elementName.equals("p4-checkout"))
        {
            command = loadP4CheckoutCommand(context, childElements.get(0), commandCommon);            
        }
        else if (elementName.equals("svn-checkout"))
        {
            command = loadSVNCheckoutCommand(context, childElements.get(0), commandCommon);
        }
        commandCommon.setCommand(command);
        
        for (ArtifactSpec artifactSpec : command.getArtifacts())
        {
            commandCommon.addArtifactSpec(artifactSpec);
        }

        for (int i = 1; i < childElements.size(); i++)
        {
            Element child = childElements.get(i);
            String  childName = child.getLocalName();

            if (childName.equals(COMMAND_CONFIG_ELEMENT_ARTIFACT))
            {
                loadArtifact(commandCommon, context, child);
            } else if (childName.equals(COMMAND_CONFIG_ELEMENT_PROCESS))
            {
                loadProcess(commandCommon, context, child, project);
            } else
            {
                throw new ConfigException(context.getFilename(), "Command element includes unrecognised element '" + childName + "'");
            }
        }
        return commandCommon;
    }

    private Command loadSVNCheckoutCommand(ConfigContext context, Element element, CommandCommon commandCommon) throws ConfigException
    {
        SVNCheckoutCommand command = new SVNCheckoutCommand(commandCommon);
        command.setUser(XMLConfigUtils.getAttributeValue(context, element, SVN_CONFIG_ATTR_USER, context.getVariableValue(SVN_VARIABLE_USER)));
        command.setPassword(XMLConfigUtils.getAttributeValue(context, element, SVN_CONFIG_ATTR_PASSWORD, context.getVariableValue(SVN_VARIABLE_PASSWORD)));
        command.setKeyFile(XMLConfigUtils.getOptionalAttributeValue(context, element, SVN_CONFIG_ATTR_KEY_FILE, context.getVariableValue(SVN_VARIABLE_KEY_FILE)));
        command.setPassphrase(XMLConfigUtils.getOptionalAttributeValue(context, element, SVN_CONFIG_ATTR_PASSPHRASE, context.getVariableValue(SVN_VARIABLE_PASSPHRASE)));
        command.setUrl(XMLConfigUtils.getAttributeValue(context, element, SVN_CONFIG_ATTR_URL, context.getVariableValue(SVN_VARIABLE_URL)));
        command.setPath(new File(XMLConfigUtils.getAttributeValue(context, element, SVN_CONFIG_ATTR_PATH)));
        return command;
    }

    private Command loadP4CheckoutCommand(ConfigContext context, Element element, CommandCommon commandCommon) throws ConfigException
    {
        P4CheckoutCommand command = new P4CheckoutCommand(commandCommon);        
        command.setPort(XMLConfigUtils.getOptionalAttributeValue(context, element, P4_CONFIG_ATTR_PORT, context.getVariableValue(P4_VARIABLE_PORT)));
        command.setUser(XMLConfigUtils.getOptionalAttributeValue(context, element, P4_CONFIG_ATTR_USER, context.getVariableValue(P4_VARIABLE_USER)));
        command.setPassword(XMLConfigUtils.getOptionalAttributeValue(context, element, P4_CONFIG_ATTR_PASSWORD, context.getVariableValue(P4_VARIABLE_PASSWORD)));
        command.setClient(XMLConfigUtils.getOptionalAttributeValue(context, element, P4_CONFIG_ATTR_CLIENT, context.getVariableValue(P4_VARIABLE_CLIENT)));
        command.setPath(new File(XMLConfigUtils.getAttributeValue(context, element, P4_CONFIG_ATTR_PATH)));        
        return command;
    }

    private void loadArtifact(CommandCommon command, ConfigContext context, Element element) throws ConfigException
    {
        command.addArtifactSpec(loadArtifactSpec(context, element));
    }

    private ArtifactSpec loadArtifactSpec(ConfigContext context, Element element) throws ConfigException
    {
        ArtifactSpec spec = new ArtifactSpec();
        spec.setName(XMLConfigUtils.getAttributeValue(context, element, ARTIFACT_CONFIG_ATTR_NAME));        
        spec.setTitle(XMLConfigUtils.getAttributeValue(context, element, ARTIFACT_CONFIG_ATTR_TITLE, spec.getName()));
        spec.setType(XMLConfigUtils.getAttributeValue(context, element, ARTIFACT_CONFIG_ATTR_TYPE));
        spec.setFromFile(new File(XMLConfigUtils.getAttributeValue(context, element, ARTIFACT_CONFIG_ATTR_FROM_FILE)));
        String toFileName = XMLConfigUtils.getAttributeValue(context, element, ARTIFACT_CONFIG_ATTR_TO_FILE, spec.getFromFile().getName());
        spec.setToFile(new File(toFileName));
        return spec;
    }

    private void loadProcess(CommandCommon command, ConfigContext context, Element element, Project project) throws ConfigException
    {
        command.addProcessSpec(loadProcessSpec(command, context, element, project));
    }

    private ProcessSpec loadProcessSpec(CommandCommon command, ConfigContext context, Element element, Project project) throws ConfigException
    {
        ProcessSpec spec = new ProcessSpec();
        String processorName = XMLConfigUtils.getAttributeValue(context, element, PROCESS_CONFIG_ATTR_PROCESSOR);
        String artifactName  = XMLConfigUtils.getAttributeValue(context, element, PROCESS_CONFIG_ATTR_ARTIFACT);
        
        if(project.hasPostProcessor(processorName))
        {
            spec.setProcessor(project.getPostProcessor(processorName));
        }
        else
        {
            throw new ConfigException(context.getFilename(), "Command '" + command.getName() + "' process directive refers to unknown post-processor '" + processorName + "'");
        }
        
        if(command.hasArtifact(artifactName))
        {
            spec.setArtifact(command.getArtifact(artifactName));
            if(!spec.getProcessor().getProcessor().understandsType(spec.getArtifact().getType()))
            {
                throw new ConfigException(context.getFilename(), "Command '" + command.getName() + "': post-processor '" + processorName + "' does not understand type of artifact '" + artifactName + "' (" + spec.getArtifact().getType() + ")");
            }
        }
        else
        {
            throw new ConfigException(context.getFilename(), "Command '" + command.getName() + "' process directive refers to unknown artifact '" + artifactName + "'");            
        }

        return null;
    }

    /**
     * @param context
     * @param element
     * @throws ConfigException
     */
    private void loadSchedule(Project project, ConfigContext context, Element element) throws ConfigException
    {
        Schedule s = new Schedule();
        s.setFrequency(XMLConfigUtils.getAttributeValue(context, element, "frequency"));
        project.schedules.add(s);
    }

    private void addBuiltinVariables(Project project, ConfigContext context)
    {
        context.setVariable(VARIABLE_WORK_DIR, getBuildManager().getWorkRoot(project).getAbsolutePath());
    }

    private void loadConfig(Project project, String filename) throws ConfigException
    {
        Document      doc = XMLConfigUtils.loadFile(filename);
        ConfigContext context = new ConfigContext(filename);

        addBuiltinVariables(project, context);

        List<Element> elements = XMLConfigUtils.getElements(context, doc.getRootElement(), Arrays.asList(XMLConfigUtils.CONFIG_ELEMENT_PROPERTY, CONFIG_ELEMENT_DESCRIPTION, CONFIG_ELEMENT_POST_PROCESSOR, CONFIG_ELEMENT_RECIPE, CONFIG_ELEMENT_SCHEDULE));

        XMLConfigUtils.extractProperties(context, elements);

        for (Element current : elements)
        {
            String  elementName = current.getLocalName();

            if (elementName.equals(CONFIG_ELEMENT_DESCRIPTION))
            {
                loadDescription(project, context, current);
            } else if (elementName.equals(CONFIG_ELEMENT_POST_PROCESSOR))
            {
                loadPostProcessor(project, context, current);
            } else if (elementName.equals(CONFIG_ELEMENT_RECIPE))
            {
                loadRecipe(project, context, current);
            } else if (elementName.equals(CONFIG_ELEMENT_SCHEDULE))
            {
                loadSchedule(project, context, current);
            } else
            {
                assert(false);
            }
        }

        project.schedule();
    }

    
    private Command loadExeConfig(CommandCommon common, ConfigContext context, Element element) throws ConfigException
    {
        ExecutableCommand exe = new ExecutableCommand();
        exe.setCommandCommon(common);
        exe.setExecutable(XMLConfigUtils.getAttributeValue(context, element, EXE_CONFIG_ATTR_EXECUTABLE));
        String working = XMLConfigUtils.getAttributeValue(context, element, EXE_CONFIG_ATTR_WORKING_DIRECTORY, ".");
        exe.setWorkingDirectory(new File(working));
        
        String argumentString = XMLConfigUtils.getOptionalAttributeValue(context, element, EXE_CONFIG_ATTR_ARGUMENTS, null);
        List<String> command;

        if(argumentString == null)
            command = new LinkedList<String>();
        else
        {
            exe.setArguments(argumentString.split(" "));
            command = new LinkedList<String>(Arrays.asList(exe.getArguments()));
        }

        command.add(0, exe.getExecutable());
        exe.setCommand(command);

        loadExeChildElements(exe, context, element);
        return exe;
    }

    private void loadExeChildElements(ExecutableCommand exe, ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(EXE_CONFIG_ELEMENT_ENVIRONMENT));
        
        for(Element current: elements)
        {
            loadExeEnvironment(exe, context, current);
        }        
    }

    
    private void loadExeEnvironment(ExecutableCommand exe, ConfigContext context, Element element) throws ConfigException
    {
        String name  = XMLConfigUtils.getAttributeValue(context, element, EXE_CONFIG_ATTR_NAME);
        String value = XMLConfigUtils.getAttributeValue(context, element, EXE_CONFIG_ATTR_VALUE);
        
        exe.addEnv(name, value);
    }

    
    
    private BuildManager getBuildManager()
    {
        return BuildManager.getInstance();
    }
}
