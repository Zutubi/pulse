package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.io.File;
import java.util.List;

/**
 */
public interface CommandContext
{
    ExecutionContext getExecutionContext();

    ResultState getResultState();

    void failure(String message);

    void error(String message);

    void addFeature(Feature feature);

    void addCommandProperty(String name, String value);

    void registerLink(String name, String url);
    
    File registerOutput(String name, String type);

    void setOutputIndex(String name, String index);
    
    void processOutput(String name, List<PostProcessorConfiguration> postProcessors);

}
