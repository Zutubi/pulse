package com.zutubi.pulse.dev.expand;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.dev.util.OptionUtils;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Holds options used to control the {@link PulseFileExpander}.
 */
public class PulseFileExpanderOptions
{
    private String pulseFile;
    private String recipe;
    private Properties defines = new Properties();
    private String resourcesFile;
    private File baseDir;
    private OutputStream outputStream = System.out;
    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();

    /**
     * Creates new, default options.
     */
    public PulseFileExpanderOptions()
    {
    }

    /**
     * Creates options by parsing a command line.  The shape of the command
     * line is documented in {@link ExpandCommand}.
     * 
     * @param argv the command line to parse
     * @throws PulseException if an option in invalid or missing
     */
    @SuppressWarnings({"AccessStaticViaInstance"})
    public PulseFileExpanderOptions(String... argv) throws PulseException
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("recipe")
                .hasArg()
                .create('r'));

        options.addOption(OptionBuilder.withLongOpt("define")
                .hasArg()
                .create('d'));

        options.addOption(OptionBuilder.withLongOpt("require")
                .hasArg()
                .create('q'));

        options.addOption(OptionBuilder.withLongOpt("resources-file")
                .hasArg()
                .create('e'));

        options.addOption(OptionBuilder.withLongOpt("base-dir")
                .hasArg()
                .create('b'));
        
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine;
        try
        {
            commandLine = parser.parse(options, argv, false);

            if (commandLine.hasOption('r'))
            {
                recipe = commandLine.getOptionValue('r');
            }
    
            if (commandLine.hasOption('d'))
            {
                String[] values = commandLine.getOptionValues('d');
                for (String value: values)
                {
                    OptionUtils.addDefinedOption(value, defines);
                }
            }
            
            if (commandLine.hasOption('q'))
            {
                for (String value: commandLine.getOptionValues('q'))
                {
                    resourceRequirements.add(OptionUtils.parseResourceRequirement(value));
                }
            }
    
            if (commandLine.hasOption('e'))
            {
                resourcesFile = commandLine.getOptionValue('e');
            }
    
            if (commandLine.hasOption('b'))
            {
                baseDir = new File(commandLine.getOptionValue('b'));
            }
            else
            {
                baseDir = FileSystemUtils.getWorkingDirectory();
            }
        }
        catch (ParseException e)
        {
            throw new PulseException(e.getMessage(), e);
        }

        String[] args = commandLine.getArgs();
        if (args.length == 0)
        {
            throw new PulseException("No pulse file specified.");
        }
        
        pulseFile = args[0];
    }

    /**
     * Returns the path of the pulse file to expand.
     * 
     * @return path of the pulse file to expand, either absolute or relative to
     *         the working directory
     */
    public String getPulseFile()
    {
        return pulseFile;
    }

    public void setPulseFile(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }

    /**
     * If set, gives the name of the recipe to restrict the output to.  Only
     * the named recipe will appear in the expanded output.
     * 
     * @return name of the recipe to isolate the output to; if unset no such
     *         isolation is performed
     */
    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    /**
     * Returns a mapping of names to values for simple properties to define
     * before loading the pulse file.  These defines are processed after
     * resource requirements.
     * 
     * @return a set of properties to define before loading the pulse file
     */
    public Properties getDefines()
    {
        return defines;
    }

    /**
     * If set, gives the path of a resources file to load a resource repository
     * from.  This can be used in conjunction with requirements.
     * 
     * @return path of a resources file to load, either absolute or relative to
     *         the working directory; may be unset to disable resource loading
     */
    public String getResourcesFile()
    {
        return resourcesFile;
    }

    public void setResourcesFile(String resourcesFile)
    {
        this.resourcesFile = resourcesFile;
    }

    /**
     * Gives the base directory to be used for resolving file paths (e.g. in
     * imports).
     * 
     * @return the base directory to use
     */
    public File getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
    }

    /**
     * Returns a list of resource requirements to apply before loading the
     * pulse file.  These requirements may introduce values into the scope.
     * 
     * @return a list of resource requirements to apply
     */
    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    /**
     * Returns a stream to write the expanded Pulse file to.  This stream is
     * not owned by these options or the expanded - so it will not be closed
     * by either.  Defaults to System.out.
     * 
     * @return output stream to write the expanded file to
     */
    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }
}
