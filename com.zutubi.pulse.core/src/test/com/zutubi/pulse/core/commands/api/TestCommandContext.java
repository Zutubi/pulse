package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.*;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Pair;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link com.zutubi.pulse.core.commands.api.CommandContext}
 * useful for testing.  Records what the command does to the context for later
 * inspection.
 */
public class TestCommandContext implements CommandContext
{
    private ExecutionContext executionContext;
    private ResultState resultState = ResultState.IN_PROGRESS;
    private List<Feature> features = new LinkedList<Feature>();
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, String> links = new HashMap<String, String>();
    private Map<String, Output> outputs = new HashMap<String, Output>();
    private Map<Pair<FieldScope, String>, String> customFields = new HashMap<Pair<FieldScope, String>, String>();

    /**
     * Create a new context that will return the given execution context from
     * {@link #getExecutionContext()}.
     *
     * @param executionContext the context in which the command executes
     */
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

    /**
     * Returns the features registered against this context.
     *
     * @return all features added to this context
     */
    public List<Feature> getFeatures()
    {
        return features;
    }

    /**
     * Returns the features of the given level registered against this context.
     *
     * @param level the level of feature to retrieve
     * @return all features of the given level added to this context
     */
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

    /**
     * Returns all properties registered via {@link #addCommandProperty(String, String)}.
     *
     * @return all registered properties
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * Returns all links registered via {@link #registerLink(String, String)}.
     *
     * @return all registered link outputs
     */
    public Map<String, String> getLinks()
    {
        return links;
    }

    /**
     * Returns all outputs registered via {@link #registerOutput(String, String)}.
     * These output instances in turn include details of processors etc
     * registered against them.
     *
     * @return all registered outputs
     * @see Output
     */
    public Map<String, Output> getOutputs()
    {
        return outputs;
    }

    /**
     * Returns all custom fields added via {@link #addCustomField(com.zutubi.pulse.core.engine.api.FieldScope, String, String)}.
     * The map keys are (scope, name) pairs and the map values are the property
     * values.
     *
     * @return all added custom fields
     */
    public Map<Pair<FieldScope, String>, String> getCustomFields()
    {
        return customFields;
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

    public void markOutputForPublish(String name, String pattern)
    {
        outputs.get(name).setPublish(true);
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

    public void addCustomField(FieldScope scope, String name, String value)
    {
        customFields.put(asPair(scope, name), value);
    }

    public void registerProcessors(String name, List<PostProcessorConfiguration> postProcessors)
    {
        Output output = outputs.get(name);
        if (output != null)
        {
            output.applyProcessors(postProcessors);
        }
    }

    /**
     * Called when the command completes, updating the state if necessary.
     */
    public void complete()
    {
        if (resultState == ResultState.IN_PROGRESS)
        {
            resultState = ResultState.SUCCESS;
        }
    }

    /**
     * Records information about a registered output.
     *
     * @see TestCommandContext#registerOutput(String, String)
     */
    public static class Output
    {
        private String name;
        private String index;
        private List<PostProcessorConfiguration> appliedProcessors = new LinkedList<PostProcessorConfiguration>();
        private boolean publish;

        /**
         * Creates an output of the given name.
         *
         * @param name the name of the output
         */
        public Output(String name)
        {
            this.name = name;
        }

        /**
         * Returns this output's name.
         *
         * @return the name of the output
         */
        public String getName()
        {
            return name;
        }

        /**
         * Returns the index, which will only be set if a call to
         * {@link TestCommandContext#setOutputIndex(String, String)} was made
         * for this output.
         *
         * @return the index set for this output (may be null)
         */
        public String getIndex()
        {
            return index;
        }

        /**
         * Sets the index file path for this output.  Used for setting up
         * expected outputs.
         *
         * @param index the index for this output
         * @see #getIndex()
         */
        public void setIndex(String index)
        {
            this.index = index;
        }

        /**
         * Returns all processors registered against this output via
         * {@link TestCommandContext#registerProcessors(String, java.util.List)}.
         *
         * @return all processors registered against this output
         */
        public List<PostProcessorConfiguration> getAppliedProcessors()
        {
            return appliedProcessors;
        }

        /**
         * Adds registered processors to this output.  Used for setting up
         * expected outputs.
         *
         * @param processors the processors to register
         */
        public void applyProcessors(List<PostProcessorConfiguration> processors)
        {
            appliedProcessors.addAll(processors);
        }

        public boolean isPublish()
        {
            return publish;
        }

        public void setPublish(boolean b)
        {
            publish = b;
        }
    }
}
