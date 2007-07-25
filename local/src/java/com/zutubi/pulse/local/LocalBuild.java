package com.zutubi.pulse.local;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.resources.ResourceDiscoverer;
import com.zutubi.pulse.util.IOUtils;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.List;

/**
 * Entry point for executing local builds within a development tree.
 */
public class LocalBuild
{
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


        CommandLineParser parser = new PosixParser();

        try
        {
            CommandLine commandLine = parser.parse(options, argv, true);

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

            LocalBuild b = new LocalBuild();
            File baseDir = new File(System.getProperty("user.dir"));
            b.runBuild(baseDir, pulseFile, recipe, resourcesFile, outputDir);
        }
        catch (Exception e)
        {
            fatal(e);
        }
    }

    private FileResourceRepository createRepository(String resourcesFile) throws PulseException
    {
        if (resourcesFile == null)
        {
            return new FileResourceRepository();
        }

        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream(resourcesFile);
            return ResourceFileLoader.load(stream);
        }
        catch (FileNotFoundException e)
        {
            throw new PulseException("Unable to open resources file '" + resourcesFile + "'");
        }
        finally
        {
            IOUtils.close(stream);
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
     * @throws PulseException
     */
    public void runBuild(File baseDir, String pulseFileName, String recipe, String resourcesFile, String outputDir) throws PulseException
    {
        printPrologue(pulseFileName, resourcesFile, outputDir);

        FileResourceRepository repository = createRepository(resourcesFile);
        discoverResources(repository);

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

            // manually write these components.
            EventManager manager = new DefaultEventManager();
            ObjectFactory objectFactory = new ObjectFactory();
            FileLoader fileLoader = new PulseFileLoader();
            fileLoader.setObjectFactory(objectFactory);

            manager.register(new BuildStatusPrinter(paths.getBaseDir(), logStream));

            Bootstrapper bootstrapper = new LocalBootstrapper();
            RecipeProcessor processor = new RecipeProcessor();
            processor.setEventManager(manager);
            processor.setFileLoader(fileLoader);

            RecipeRequest request = new RecipeRequest(0, bootstrapper, loadPulseFile(baseDir, pulseFileName), recipe);
            processor.build(request, paths, repository, false, null);
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

    private void discoverResources(FileResourceRepository repository)
    {
        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        List<Resource> resources = discoverer.discover();
        for(Resource r: resources)
        {
            if(!repository.hasResource(r.getName()))
            {
                repository.addResource(r);
            }
        }
    }

    private String loadPulseFile(File baseDir, String pulseFileName) throws PulseException
    {
        File pulseFile = new File(baseDir, pulseFileName);
        FileInputStream pulseFileInputStream = null;
        String result;

        try
        {
            pulseFileInputStream = new FileInputStream(pulseFile);
            result = IOUtils.inputStreamToString(pulseFileInputStream);
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to load pulse file '" + pulseFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(pulseFileInputStream);
        }

        return result;
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
}
