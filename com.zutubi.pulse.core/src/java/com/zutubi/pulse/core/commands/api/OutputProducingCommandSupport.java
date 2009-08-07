package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.ForkOutputStream;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.IgnoreCloseOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper base class for commands that produce output.  The output is captured
 * using a standard name ("command output") and file ("output.txt").  Also
 * provides the capability to store the output in a file within the working
 * directory (similar to redirecting output in a shell).
 */
public abstract class OutputProducingCommandSupport extends CommandSupport
{
    public static final String OUTPUT_NAME = "command output";
    public static final String OUTPUT_FILE = "output.txt";

    private PostProcessorExtensionManager postProcessorExtensionManager;

    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this command
     * @see #getConfig() 
     */
    protected OutputProducingCommandSupport(OutputProducingCommandConfigurationSupport config)
    {
        super(config);
    }

    @Override
    public OutputProducingCommandConfigurationSupport getConfig()
    {
        return (OutputProducingCommandConfigurationSupport) super.getConfig();
    }

    /**
     * Override this method to indicate which post-processors should be applied
     * by default to the output.  For example, a MakeCommand might apply a
     * MakePostProcessor (to find make error messages) by default.
     *
     * @return types of post-processors to apply to the output (none by
     *         default)
     */
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Collections.emptyList();
    }

    /**
     * Override this method to directly provide default post-processors to
     * apply to the output.  Usually it is better (and simpler) to override
     * {@link #getDefaultPostProcessorTypes()} and leave the remaining details
     * to this implementation.  Only override this method when full control is
     * required.
     *
     * @param commandContext the context in which the command is executing
     * @return post-processors to apply to the output (by default based on the
     *         types returned by {@link #getDefaultPostProcessorTypes()})
     */
    protected List<PostProcessorConfiguration> getDefaultPostProcessors(CommandContext commandContext)
    {
        List<PostProcessorConfiguration> result = new LinkedList<PostProcessorConfiguration>();
        ExecutionContext executionContext = commandContext.getExecutionContext();
        for (Class<? extends PostProcessorConfiguration> type: getDefaultPostProcessorTypes())
        {
            String name = postProcessorExtensionManager.getDefaultProcessorName(type);
            if (name != null)
            {
                PostProcessorConfiguration processor = executionContext.getValue(name, PostProcessorConfiguration.class);
                if (processor != null)
                {
                    result.add(processor);
                }
            }
        }

        return result;
    }
    
    public void execute(CommandContext commandContext)
    {
        File outputDir = commandContext.registerArtifact(OUTPUT_NAME, null);
        File outputArtifact = new File(outputDir, OUTPUT_FILE);

        OutputStream stream = null;
        try
        {
            stream = getOutputStream(commandContext.getExecutionContext(), outputArtifact);
            execute(commandContext, stream);
        }
        catch (FileNotFoundException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(stream);

            // Even on error, process whatever we have captured.
            commandContext.registerProcessors(OUTPUT_NAME, getDefaultPostProcessors(commandContext));
            commandContext.registerProcessors(OUTPUT_NAME, getConfig().getPostProcessors());
        }
    }

    private OutputStream getOutputStream(ExecutionContext context, File outputArtifact) throws FileNotFoundException
    {
        List<OutputStream> outputs = new ArrayList<OutputStream>(3);
        outputs.add(new FileOutputStream(outputArtifact));

        if (context.getOutputStream() != null)
        {
            // Wrap in an ignore close stream as we don't own this stream and
            // thus don't want to close it with the rest when done.
            outputs.add(new IgnoreCloseOutputStream(context.getOutputStream()));
        }

        String outputFile = getConfig().getOutputFile();
        if (StringUtils.stringSet(outputFile))
        {
            try
            {
                outputs.add(new FileOutputStream(new File(getWorkingDir(context.getWorkingDir()), outputFile)));
            }
            catch (FileNotFoundException e)
            {
                throw new BuildException("Unable to create output file '" + outputFile + "': " + e.getMessage(), e);
            }
        }

        OutputStream output;
        if (outputs.size() > 1)
        {
            output = new ForkOutputStream(outputs.toArray(new OutputStream[outputs.size()]));
        }
        else
        {
            output = outputs.get(0);
        }
        return output;
    }

    /**
     * Override this method to indicate the working directory for the command,
     * if it is not always the base directory.
     *
     * @param baseDir the base directory for the build
     * @return the working directory for the command
     */
    protected File getWorkingDir(File baseDir)
    {
        return baseDir;
    }

    /**
     * Abstract execution method which extends the usual {@link #execute(CommandContext)}
     * method with the addition of an output stream parameter.  The
     * implementation should write all command output to this stream.
     *
     * @param commandContext context in which the command is executing
     * @param outputStream   stream to which all command output should be
     *                       written
     */
    protected abstract void execute(CommandContext commandContext, OutputStream outputStream);

    public void setPostProcessorExtensionManager(PostProcessorExtensionManager postProcessorExtensionManager)
    {
        this.postProcessorExtensionManager = postProcessorExtensionManager;
    }
}
