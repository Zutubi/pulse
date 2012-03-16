package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.*;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zutubi.util.CollectionUtils.asPair;

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
    private Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
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

    public void warning(String message)
    {
        addFeature(new Feature(Feature.Level.WARNING, message));
        resultState = ResultState.WARNINGS;
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
     * Returns all links registered via {@link CommandContext#registerLink(String, String,boolean,boolean)}.
     *
     * @return all registered link artifacts
     */
    public Map<String, String> getLinks()
    {
        return links;
    }

    /**
     * Returns all artifacts registered via {@link CommandContext#registerArtifact(String, String,boolean,boolean, com.zutubi.pulse.core.commands.api.CommandContext.HashAlgorithm)}.
     * These artifact instances in turn include details of processors etc
     * registered against them.
     *
     * @return all registered artifacts
     * @see com.zutubi.pulse.core.commands.api.TestCommandContext.Artifact
     */
    public Map<String, Artifact> getArtifacts()
    {
        return artifacts;
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

    public void registerLink(String name, String url, boolean explicit, boolean featured)
    {
        links.put(name, url);
    }

    public void markArtifactForPublish(String name, String pattern)
    {
        artifacts.get(name).setPublish(true);
    }

    public File registerArtifact(String name, String type, boolean explicit, boolean featured, HashAlgorithm hashAlgorithm)
    {
        File toDir = new File(executionContext.getFile(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR), name);
        if (!toDir.mkdirs())
        {
            throw new BuildException("Unable to create storage directory '" + toDir.getAbsolutePath() + "' for artifact '" + name + "'");
        }

        artifacts.put(name, new Artifact(name));
        return toDir;
    }

    public void setArtifactIndex(String name, String index)
    {
        Artifact artifact = artifacts.get(name);
        if (artifact == null)
        {
            throw new BuildException("Attempt to set index file for unknown artifact '" + name + "'");
        }

        artifact.setIndex(index);
    }

    public void addCustomField(FieldScope scope, String name, String value)
    {
        customFields.put(asPair(scope, name), value);
    }

    public void registerProcessors(String name, List<PostProcessorConfiguration> postProcessors)
    {
        Artifact artifact = artifacts.get(name);
        if (artifact != null)
        {
            artifact.applyProcessors(postProcessors);
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
     * Records information about a registered artifact.
     *
     * @see CommandContext#registerArtifact(String, String,boolean,boolean, com.zutubi.pulse.core.commands.api.CommandContext.HashAlgorithm)
     */
    public static class Artifact
    {
        private String name;
        private String index;
        private List<PostProcessorConfiguration> appliedProcessors = new LinkedList<PostProcessorConfiguration>();
        private boolean publish;

        /**
         * Creates an artifact of the given name.
         *
         * @param name the name of the artifact
         */
        public Artifact(String name)
        {
            this.name = name;
        }

        /**
         * Returns this artifact's name.
         *
         * @return the name of the artifact
         */
        public String getName()
        {
            return name;
        }

        /**
         * Returns the index, which will only be set if a call to
         * {@link TestCommandContext#setArtifactIndex(String, String)} was made
         * for this artifact.
         *
         * @return the index set for this artifact (may be null)
         */
        public String getIndex()
        {
            return index;
        }

        /**
         * Sets the index file path for this artifact.  Used for setting up
         * expected artifacts.
         *
         * @param index the index for this artifact
         * @see #getIndex()
         */
        public void setIndex(String index)
        {
            this.index = index;
        }

        /**
         * Returns all processors registered against this artifact via
         * {@link TestCommandContext#registerProcessors(String, java.util.List)}.
         *
         * @return all processors registered against this artifact
         */
        public List<PostProcessorConfiguration> getAppliedProcessors()
        {
            return appliedProcessors;
        }

        /**
         * Adds registered processors to this artifact.  Used for setting up
         * expected artifacts.
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
