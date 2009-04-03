package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper base for configuration for commands that produce output that may be
 * captured and/or processed.
 *
 * @see com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport
 */
@SymbolicName("zutubi.outputProducingCommandConfigSupport")
public abstract class OutputProducingCommandConfigurationSupport extends CommandConfigurationSupport
{
    @Wizard.Ignore
    private String outputFile;
    @Reference @Addable(value = "process", attribute = "processor")
    private List<PostProcessorConfiguration> postProcessors = new LinkedList<PostProcessorConfiguration>();

    public OutputProducingCommandConfigurationSupport(Class<? extends Command> commandType)
    {
        super(commandType);
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile(String outputFile)
    {
        this.outputFile = outputFile;
    }

    public List<PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        postProcessors.add(postProcessor);
    }
}
