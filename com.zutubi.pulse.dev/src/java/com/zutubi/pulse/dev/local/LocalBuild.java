package com.zutubi.pulse.dev.local;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ExternalPulseFileSource;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Entry point for executing local builds within a development tree.
 */
public class LocalBuild
{
    private static final int DEFAULT_FAILURE_LIMIT = 50;

    private int failureLimit = DEFAULT_FAILURE_LIMIT;
    private EventManager eventManager;
    private RecipeProcessor recipeProcessor;
    private ResourceFileLoader resourceFileLoader;

    @SuppressWarnings({ "ACCESS_STATIC_VIA_INSTANCE", "AccessStaticViaInstance" })
    public static void main(String argv[])
    {
        String pulseFile = "pulse.xml";
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

        options.addOption(OptionBuilder.withLongOpt("resources-file")
                .hasArg()
                .create('e'));

        options.addOption(OptionBuilder.withLongOpt("failure-limit")
                .hasArg()
                .withType(Number.class)
                .create('l'));

        LocalBuild b = bootstrap();

        try
        {
            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, argv, false);

            if (commandLine.hasOption('p'))
            {
                pulseFile = commandLine.getOptionValue('p');
            }

            if (commandLine.hasOption('r'))
            {
                recipe = commandLine.getOptionValue('r');
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

            File baseDir = new File(System.getProperty("user.dir"));
            b.runBuild(baseDir, pulseFile, recipe, resourcesFile, outputDir);
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

    public static LocalBuild bootstrap()
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/local/bootstrap/context/applicationContext.xml");
        return SpringComponentContext.getBean("localBuild");
    }

    private InMemoryResourceRepository createRepository(String resourcesFile) throws PulseException
    {
        if (resourcesFile == null)
        {
            return new InMemoryResourceRepository();
        }

        try
        {
            return resourceFileLoader.load(new File(resourcesFile));
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
    public void runBuild(File baseDir, String pulseFileName, String recipe, String resourcesFile, String outputDir) throws PulseException
    {
        printPrologue(pulseFileName, resourcesFile, outputDir);

        InMemoryResourceRepository repository = createRepository(resourcesFile);
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        discoverer.discoverAndAdd(repository);

        RecipePaths paths = new LocalRecipePaths(baseDir, outputDir);

        if (!paths.getBaseDir().isDirectory())
        {
            throw new PulseException("Base directory '" + paths.getBaseDir().getAbsolutePath() + "' does not exist");
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
            RecipeRequest request = new RecipeRequest(bootstrapper, new ExternalPulseFileSource(pulseFileName), context);
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

    public void setResourceFileLoader(ResourceFileLoader resourceFileLoader)
    {
        this.resourceFileLoader = resourceFileLoader;
    }
}
