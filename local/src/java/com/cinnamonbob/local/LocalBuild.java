package com.cinnamonbob.local;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.cinnamonbob.core.*;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.event.DefaultEventManager;
import com.cinnamonbob.core.model.BuildResult;

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
            if(commandLine.hasOption('b'))
            {
                bobFile = commandLine.getOptionValue('b');
            }

            if(commandLine.hasOption('r'))
            {
                resourcesFile = commandLine.getOptionValue('r');
            }

            if(commandLine.hasOption('o'))
            {
                outputDir = commandLine.getOptionValue('o');
            }

            argv = commandLine.getArgs();
            if(argv.length > 0)
            {
                recipe = argv[0];
            }

            LocalBuild b = new LocalBuild();
            b.runBuild(bobFile, recipe, resourcesFile, outputDir);
        }
        catch (Exception e)
        {
            fatal(e);
        }
    }

    private ResourceRepository createRepository(String resourcesFile) throws BobException
    {
        FileLoader loader = new FileLoader(new ObjectFactory(), null);
        ResourceRepository repository = new ResourceRepository(loader);

        if(resourcesFile != null)
        {
            try
            {
                FileInputStream stream = new FileInputStream(resourcesFile);
                repository.load(stream);
            }
            catch (FileNotFoundException e)
            {
                throw new BobException("Unable to open resources file '" + resourcesFile + "'");
            }
        }

        return repository;
    }

    private void runBuild(String bobFile, String recipe, String resourcesFile, String outputDir) throws Exception
    {
        printPrologue(bobFile, resourcesFile, outputDir);

        EventManager manager = new DefaultEventManager();
        manager.register(new BuildStatusPrinter());

        ResourceRepository repository = createRepository(resourcesFile);

        File workDir = new File(System.getProperty("user.dir"));
        File output = new File(workDir, outputDir);

        BuildResult result = new BuildResult(bobFile, 0);
        result.commence(output);
        manager.publish(new BuildCommencedEvent(this, result));

        try
        {
            cleanOutputDir(output);

            FileLoader loader = new FileLoader(new ObjectFactory(), repository);
            BuildProcessor processor = new BuildProcessor(manager, loader);
            processor.build(workDir, bobFile, recipe, result, output);
        }
        catch(BuildException e)
        {
            result.error(e);
        }
        catch(Exception e)
        {
            result.error(new BuildException(e));
        }
        finally
        {
            result.complete();
            manager.publish(new BuildCompletedEvent(this, result));
        }
    }

    private void printPrologue(String bobFile, String resourcesFile, String outputDir)
    {
        System.out.println("bobfile         : '" + bobFile + "'");
        System.out.println("output directory: '" + outputDir + "'");

        if(resourcesFile != null)
        {
            System.out.println("resources file  : '" + resourcesFile + "'");
        }

        System.out.println();
    }

    private void cleanOutputDir(File output)
    {
        if(output.isDirectory())
        {
            if(!FileSystemUtils.removeDirectory(output))
            {
                throw new BuildException("Unable to remove existing output directory '" + output.getPath() + "'");
            }
        }

        if(!output.mkdirs())
        {
            throw new BuildException("Unable to create output directory '" + output.getPath() + "'");
        }
    }

    private static void fatal(Throwable throwable)
    {
        System.err.println(throwable.getMessage());
        System.exit(1);
    }
}
