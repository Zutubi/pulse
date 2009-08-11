package com.zutubi.pulse.dev.local;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.PulseFileSource;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Entry point for executing local builds within a development tree.
 */
public class LocalBuild
{
    private static final int DEFAULT_FAILURE_LIMIT = 50;

    private int failureLimit = DEFAULT_FAILURE_LIMIT;
    private EventManager eventManager;
    private RecipeProcessor recipeProcessor;

    @SuppressWarnings({ "ACCESS_STATIC_VIA_INSTANCE", "AccessStaticViaInstance" })
    public static void main(String argv[])
    {
        String pulseFile = "pulse.xml";
        List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();
        String resourcesFile = null;
        String outputDir = "pulse.out";
        String recipe = null;

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

        options.addOption(OptionBuilder.withLongOpt("base-dir")
                .hasArg()
                .create('b'));

        LocalBuild b = bootstrap();

        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine commandLine = parser.parse(options, argv, true);

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
                    resourceRequirements.add(parseResourceRequirement(value));
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
                b.setFailureLimit(((Number) commandLine.getOptionObject('l')).intValue());
            }

            File baseDir;
            if(commandLine.hasOption('b'))
            {
                String baseName = commandLine.getOptionValue('b');
                baseDir = new File(baseName);
                if (!baseDir.isDirectory())
                {
                    System.err.println("Base directory specified '" + baseName + "' is not a directory");
                    System.exit(1);
                }
            }
            else
            {
                baseDir = new File(System.getProperty("user.dir"));
            }

            b.runBuild(baseDir, pulseFile, recipe, resourceRequirements, resourcesFile, outputDir);
        }
        catch (Exception e)
        {
            fatal(e);
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
    }

    private static ResourceRequirement parseResourceRequirement(String arg) throws PulseException
    {
        int slashOffset = arg.indexOf('/');
        if (slashOffset < 0)
        {
            return new ResourceRequirement(arg, null, true);
        }
        else if (slashOffset == arg.length() - 1)
        {
            return new ResourceRequirement(arg.substring(0, slashOffset), null, true);
        }
        else
        {
            String name = arg.substring(0, slashOffset);
            if (!TextUtils.stringSet(name))
            {
                throw new PulseException("Resource requirement '" + arg + "' has empty resource name");
            }

            return new ResourceRequirement(name, arg.substring(slashOffset + 1), false);
        }
    }

    public static LocalBuild bootstrap()
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/local/bootstrap/context/applicationContext.xml");
        return SpringComponentContext.getBean("localBuild");
    }

    private FileResourceRepository loadResources(String resourcesFile) throws PulseException
    {
        if (resourcesFile == null)
        {
            return new FileResourceRepository();
        }

        try
        {
            return ResourceFileLoader.load(new File(resourcesFile));
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to read resources file '" + resourcesFile + "'");
        }
    }

    /**
     * Executes a local build with the given inputs, using the given output
     * directory to save results.  All paths provided must be absolute or
     * relative to the current working directory.
     *
     * @param baseDir       the base directory in which to execute the build
     * @param pulseFileName the name of the pulsefile to load
     * @param recipe        the recipe to execute, may be null to indicate the default
     *                      recipe in the given pulsefile
     * @param resourcesFile the resources file to load prior to building , or null if no
     *                      resources are to be loaded
     * @param outputDir     the name of the output directory to capture output
     *                      and save results to
     * @throws PulseException if a build error occurs
     */
    public void runBuild(File baseDir, String pulseFileName, String recipe, List<ResourceRequirement> resourceRequirements, String resourcesFile, String outputDir) throws PulseException
    {
        printPrologue(pulseFileName, resourcesFile, outputDir);

        FileResourceRepository repository = loadResources(resourcesFile);
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        discoverer.discoverAndAdd(repository);

        RecipePaths paths = new LocalRecipePaths(baseDir, outputDir);

        if (!paths.getBaseDir().isDirectory())
        {
            throw new PulseException("Base directory '" + paths.getBaseDir().getAbsolutePath() + "' does not exist");
        }

        try
        {
            FileSystemUtils.cleanOutputDir(paths.getOutputDir());
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to clean output directory '" + paths.getOutputDir().getAbsolutePath() + "': " + e.getMessage(), e);
        }

        File logFile = new File(baseDir, "build.log");
        FileOutputStream logStream = null;

        try
        {
            logStream = new FileOutputStream(logFile);

            eventManager.register(new BuildStatusPrinter(paths.getBaseDir(), paths.getOutputDir(), logStream, failureLimit));

            PulseExecutionContext context = new PulseExecutionContext();
            context.setWorkingDir(baseDir);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_LOCAL_BUILD, Boolean.toString(true));
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, repository);
            context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, recipe);
            Bootstrapper bootstrapper = new LocalBootstrapper();
            RecipeRequest request = new RecipeRequest(bootstrapper, loadPulseFile(baseDir, pulseFileName), context);
            request.addAllResourceRequirements(CollectionUtils.map(repository.getRequirements(), new Mapping<SimpleResourceRequirement, ResourceRequirement>()
            {
                public ResourceRequirement map(SimpleResourceRequirement rr)
                {
                    return rr.asResourceRequirement();
                }
            }));

            request.addAllResourceRequirements(resourceRequirements);
            recipeProcessor.build(request);
        }
        catch (FileNotFoundException e)
        {
            throw new PulseException("Unable to create log file '" + logFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(logStream);
        }

        printEpilogue(logFile);
    }

    private PulseFileSource loadPulseFile(File baseDir, String pulseFileName) throws PulseException
    {
        File pulseFile = new File(baseDir, pulseFileName);
        FileInputStream pulseFileInputStream = null;
        String content;

        try
        {
            pulseFileInputStream = new FileInputStream(pulseFile);
            content = IOUtils.inputStreamToString(pulseFileInputStream);
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to load pulse file '" + pulseFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(pulseFileInputStream);
        }

        return new PulseFileSource(pulseFileName, content);
    }

    private void printPrologue(String pulseFile, String resourcesFile, String outputDir)
    {
        System.out.println("pulse file      : '" + pulseFile + "'");
        System.out.println("output directory: '" + outputDir + "'");

        if (resourcesFile != null)
        {
            System.out.println("resources file  : '" + resourcesFile + "'");
        }

        System.out.println();
    }

    private void printEpilogue(File logFile)
    {
        System.out.println();
        System.out.println("Build report saved to '" + logFile.getPath() + "'.");
    }

    private static void fatal(Throwable throwable)
    {
        System.err.println(throwable.getMessage());
        System.exit(1);
    }

    public void setFailureLimit(int failureLimit)
    {
        this.failureLimit = failureLimit;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }
}
