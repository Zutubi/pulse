package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.dev.util.OptionUtils;
import org.apache.commons.cli.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates options for a local build, including the ability to parse such
 * options from a command-line.
 */
public class LocalBuildOptions
{
    public static final String DEFAULT_PULSE_FILE = "pulse.xml";
    public static final String DEFAULT_OUTPUT_DIRECTORY = "pulse.out";
    public static final int DEFAULT_FAILURE_LIMIT = 50;

    private String pulseFile = DEFAULT_PULSE_FILE;
    private String resourcesFile = null;
    private String outputDir = DEFAULT_OUTPUT_DIRECTORY;
    private String recipe = null;
    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();
    private int failureLimit = DEFAULT_FAILURE_LIMIT;
    private boolean verbose = false;

    public LocalBuildOptions()
    {
    }

    @SuppressWarnings({ "ACCESS_STATIC_VIA_INSTANCE", "AccessStaticViaInstance" })
    public LocalBuildOptions(String... argv) throws PulseException
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("output-dir")
                .hasArg()
                .create('o'));

        options.addOption(OptionBuilder.withLongOpt("pulse-file")
                .hasArg()
                .create('p'));

        options.addOption(OptionBuilder.withLongOpt("recipe")
                .hasArg()
                .create('r'));

        options.addOption(OptionBuilder.withLongOpt("require")
                .hasArg()
                .create('q'));

        options.addOption(OptionBuilder.withLongOpt("resources-file")
                .hasArg()
                .create('e'));

        options.addOption(OptionBuilder.withLongOpt("failure-limit")
                .hasArg()
                .withType(Number.class)
                .create('l'));

        options.addOption(OptionBuilder.withLongOpt("verbose")
                .create('v'));
        
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine;
        try
        {
            commandLine = parser.parse(options, argv, false);
        }
        catch (ParseException e)
        {
            throw new PulseException(e.getMessage(), e);
        }

        if (commandLine.hasOption('p'))
        {
            pulseFile = commandLine.getOptionValue('p');
        }

        if (commandLine.hasOption('r'))
        {
            recipe = commandLine.getOptionValue('r');
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

        if (commandLine.hasOption('o'))
        {
            outputDir = commandLine.getOptionValue('o');
        }

        if (commandLine.hasOption('l'))
        {
           failureLimit = ((Number) commandLine.getOptionObject('l')).intValue();
        }

        if (commandLine.hasOption('v'))
        {
           verbose = true;
        }
    }

    /**
     * @return the path of the pulse file to load
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
     * @return the resources file to load prior to building, or null if no resources are to be
     *         loaded
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
     * @return the name of the output directory to capture output and save results to
     */
    public String getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    /**
     * @return the recipe to execute, may be null to indicate the default recipe in the given pulse
     *         file
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
     * @return a list of resource that should be imported before starting the build
     */
    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements)
    {
        this.resourceRequirements = resourceRequirements;
    }

    /**
     * @return the maximum number of test failures to display
     */
    public int getFailureLimit()
    {
        return failureLimit;
    }

    public void setFailureLimit(int failureLimit)
    {
        this.failureLimit = failureLimit;
    }

    public boolean isVerbose()
    {
        return verbose;
    }
}
