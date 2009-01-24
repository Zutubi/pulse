package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class TestCommandContext implements CommandContext
{
    private ExecutionContext executionContext;
    private ResultState resultState = ResultState.IN_PROGRESS;
    private List<Feature> features = new LinkedList<Feature>();
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, String> links = new HashMap<String, String>();
    private Map<String, Output> outputs = new HashMap<String, Output>();

    public TestCommandContext(ExecutionContext executionContext)
    {
        this.executionContext = executionContext;
    }

    public ExecutionContext getExecutionContext()
    {
        return executionContext;
    }

    public ResultState getResultState()
    {
        return resultState;
    }

    public List<Feature> getFeatures()
    {
        return features;
    }

    public List<Feature> getFeatures(final Feature.Level level)
    {
        return CollectionUtils.filter(features, new Predicate<Feature>()
        {
            public boolean satisfied(Feature feature)
            {
                return feature.getLevel() == level;
            }
        });
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public Map<String, String> getLinks()
    {
        return links;
    }

    public Map<String, Output> getOutputs()
    {
        return outputs;
    }

    public void failure(String message)
    {
        addFeature(new Feature(Feature.Level.ERROR, message));
        resultState = ResultState.FAILURE;
    }

    public void error(String message)
    {
        addFeature(new Feature(Feature.Level.ERROR, message));
        resultState = ResultState.ERROR;
    }

    public void addFeature(Feature feature)
    {
        features.add(feature);
    }

    public void addCommandProperty(String name, String value)
    {
        properties.put(name, value);
    }

    public void registerLink(String name, String url)
    {
        links.put(name, url);
    }

    public File registerOutput(String name, String type)
    {
        File toDir = new File(executionContext.getFile(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR), name);
        if (!toDir.mkdirs())
        {
            throw new BuildException("Unable to create storage directory '" + toDir.getAbsolutePath() + "' for output '" + name + "'");
        }

        outputs.put(name, new Output(name));
        return toDir;
    }

    public void setOutputIndex(String name, String index)
    {
        Output output = outputs.get(name);
        if (output == null)
        {
            throw new BuildException("Attempt to set index file for unknown output '" + name + "'");
        }

        output.setIndex(index);
    }

    public void processOutput(String name, List<PostProcessorConfiguration> postProcessors)
    {
        Output output = outputs.get(name);
        if (output != null)
        {
            output.applyProcessors(postProcessors);
        }
    }

    public static class Output
    {
        private String name;
        private String index;
        private List<PostProcessorConfiguration> appliedProcessors = new LinkedList<PostProcessorConfiguration>();

        public Output(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public String getIndex()
        {
            return index;
        }

        public void setIndex(String index)
        {
            this.index = index;
        }

        public List<PostProcessorConfiguration> getAppliedProcessors()
        {
            return appliedProcessors;
        }

        public void applyProcessors(List<PostProcessorConfiguration> processors)
        {
            appliedProcessors.addAll(processors);
        }
    }
}
