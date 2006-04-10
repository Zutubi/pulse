package com.zutubi.pulse.local;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.EventManager;
import org.apache.commons.cli.*;

import java.io.*;

/**
 * Entry point for executing local builds within a development tree.
 */
public class LocalBuild
{

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
    public static void main(String argv[])
    {
        String bobFile = "bob.xml";
        String resourcesFile = null;
        String outputDir = "bob.out";
        String recipe = null;

        Options options = new Options();

        // TODO: rename bob file as appropriate
        options.addOption(OptionBuilder.withLongOpt("bob-file")
                .withArgName("file")
                .hasArg()
                .withDescription("use specified bob file [default: bob.xml]")
                .create('b'));

        options.addOption(OptionBuilder.withLongOpt("resources-file")
                .withArgName("file")
                .hasArg()
                .withDescription("use resources file [default: <none>]")
                .create('r'));

        options.addOption(OptionBuilder.withLongOpt("output-dir")
                .withArgName("dir")
                .hasArg()
                .withDescription("write output to specified directory [default: bob.out]")
                .create('o'));

        CommandLineParser parser = new PosixParser();

        try
        {
            CommandLine commandLine = parser.parse(options, argv, true);
            if (commandLine.hasOption('b'))
            {
                bobFile = commandLine.getOptionValue('b');
            }

            if (commandLine.hasOption('r'))
            {
                resourcesFile = commandLine.getOptionValue('r');
            }

            if (commandLine.hasOption('o'))
            {
                outputDir = commandLine.getOptionValue('o');
            }

            argv = commandLine.getArgs();
            if (argv.length > 0)
            {
                recipe = argv[0];
            }

            LocalBuild b = new LocalBuild();
            File baseDir = new File(System.getProperty("user.dir"));
            b.runBuild(baseDir, bobFile, recipe, resourcesFile, outputDir);
        }
        catch (Exception e)
        {
            fatal(e);
        }
    }

    private FileResourceRepository createRepository(String resourcesFile) throws BobException
    {
        if (resourcesFile == null)
        {
            return null;
        }

        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream(resourcesFile);
            return ResourceFileLoader.load(stream);
        }
        catch (FileNotFoundException e)
        {
            throw new BobException("Unable to open resources file '" + resourcesFile + "'");
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
     * @param bobFileName   the name of the bobfile to load
     * @param recipe        the recipe to execute, may be null to indicate the default
     *                      recipe in the given bobfile
     * @param resourcesFile the resources file to load prior to building , or null if no
     *                      resources are to be loaded
     * @param outputDir     the name of the output directory to capture output
     *                      and save results to
     * @throws BobException
     */
    public void runBuild(File baseDir, String bobFileName, String recipe, String resourcesFile, String outputDir) throws BobException
    {
        printPrologue(bobFileName, resourcesFile, outputDir);

        FileResourceRepository repository = createRepository(resourcesFile);
        RecipePaths paths = new LocalRecipePaths(baseDir, outputDir);

        if (!paths.getBaseDir().isDirectory())
        {
            throw new BobException("Base directory '" + paths.getBaseDir().getAbsolutePath() + "' does not exist");
        }

        File logFile = new File(baseDir, "build.log");
        FileOutputStream logStream = null;

        try
        {
            logStream = new FileOutputStream(logFile);
            EventManager manager = new DefaultEventManager();
            manager.register(new BuildStatusPrinter(paths.getBaseDir(), logStream));

            Bootstrapper bootstrapper = new LocalBootstrapper();
            RecipeProcessor processor = new RecipeProcessor();
            processor.setEventManager(manager);
            processor.setResourceRepository(repository);
            processor.init();
            processor.build(0, paths, bootstrapper, loadBobFile(baseDir, bobFileName), recipe);
        }
        catch (FileNotFoundException e)
        {
            throw new BobException("Unable to create log file '" + logFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(logStream);
        }

        printEpilogue(logFile);
    }

    private String loadBobFile(File baseDir, String bobFileName) throws BobException
    {
        File bobFile = new File(baseDir, bobFileName);
        FileInputStream bobFileInputStream = null;
        String result;

        try
        {
            bobFileInputStream = new FileInputStream(bobFile);
            result = IOUtils.inputStreamToString(bobFileInputStream);
        }
        catch (IOException e)
        {
            throw new BobException("Unable to load bob file '" + bobFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(bobFileInputStream);
        }

        return result;
    }

    private void printPrologue(String bobFile, String resourcesFile, String outputDir)
    {
        System.out.println("bobfile         : '" + bobFile + "'");
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
