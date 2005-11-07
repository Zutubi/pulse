package com.cinnamonbob.local;

import org.apache.commons.cli.*;

import java.io.File;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.BuildProcessor;
import com.cinnamonbob.core.FileLoader;
import com.cinnamonbob.core.ObjectFactory;
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
            CommandLine commandLine = parser.parse(options, argv);
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

            runBuild(bobFile, recipe, resourcesFile, outputDir);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    private static void runBuild(String bobFile, String recipe, String resourcesFile, String outputDir)
    {
        File workDir = new File(System.getProperty("user.dir"));
        File output = new File(workDir, outputDir);

        if(output.isDirectory())
        {
            if(!FileSystemUtils.removeDirectory(output))
            {
                throw new BuildException("Unable to remove existing output directory '" + outputDir + "'");
            }
        }

        if(!output.mkdirs())
        {
            throw new BuildException("Unable to create output directory '" + outputDir + "'");
        }

        EventManager manager = new DefaultEventManager();
        manager.register(new BuildStatusPrinter());

        FileLoader loader = new FileLoader(new ObjectFactory());
        BuildProcessor processor = new BuildProcessor(manager, loader);
        BuildResult result = new BuildResult("local", 0);

        processor.build(workDir, bobFile, recipe, result, output);
    }
}
