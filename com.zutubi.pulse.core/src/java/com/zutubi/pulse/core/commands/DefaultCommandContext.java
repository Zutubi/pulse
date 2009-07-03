package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.*;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_CUSTOM_FIELDS;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;
import com.zutubi.util.TextUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link com.zutubi.pulse.core.commands.api.CommandContext}.
 */
public class DefaultCommandContext implements CommandContext
{
    private ExecutionContext executionContext;
    private CommandResult result;
    private Map<String, OutputSpec> registeredOutputs = new LinkedHashMap<String, OutputSpec>();
    private PostProcessorFactory postProcessorFactory;

    public DefaultCommandContext(ExecutionContext executionContext, CommandResult result, PostProcessorFactory postProcessorFactory)
    {
        this.executionContext = executionContext;
        this.result = result;
        this.postProcessorFactory = postProcessorFactory;
    }

    public void addArtifactsToResult()
    {
        for (OutputSpec spec: registeredOutputs.values())
        {
            if (spec.artifact.getChildren().size() > 0)
            {
                result.addArtifact(spec.artifact);
            }
        }
    }
    
    public ExecutionContext getExecutionContext()
    {
        return executionContext;
    }

    public ResultState getResultState()
    {
        return result.getState();
    }

    public void failure(String message)
    {
        result.failure(message);
    }

    public void error(String message)
    {
        result.error(message);
    }

    public void addFeature(Feature feature)
    {
        if (feature.getLineNumber() == Feature.LINE_UNKNOWN)
        {
            result.addFeature(new PersistentFeature(feature.getLevel(), feature.getSummary()));
        }
        else
        {
            result.addFeature(new PersistentPlainFeature(feature.getLevel(), feature.getSummary(), feature.getFirstLine(), feature.getLastLine(), feature.getLineNumber()));
        }
    }

    public void addCommandProperty(String name, String value)
    {
        result.getProperties().put(name, value);
    }

    public void registerLink(String name, String url)
    {
        result.addArtifact(new StoredArtifact(name, url));
    }

    public File registerOutput(String name, String type)
    {
        StoredArtifact artifact = new StoredArtifact(name);
        File toDir = new File(executionContext.getFile(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR), name);
        if (!toDir.mkdirs())
        {
            throw new BuildException("Unable to create storage directory '" + toDir.getAbsolutePath() + "' for output '" + name + "'");
        }

        registeredOutputs.put(name, new OutputSpec(artifact, type, toDir));
        return toDir;
    }

    public void setOutputIndex(String name, String index)
    {
        OutputSpec spec = registeredOutputs.get(name);
        if (spec == null)
        {
            throw new BuildException("Attempt to set index file for unknown output '" + name + "'");
        }

        spec.artifact.setIndex(index);
    }

    public void addCustomField(FieldScope scope, String name, String value)
    {
        if (!TextUtils.stringSet(name))
        {
            throw new IllegalArgumentException("Name must be specified");
        }

        @SuppressWarnings({"unchecked"})
        Map<Pair<FieldScope, String>, String> fields = executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_CUSTOM_FIELDS, Map.class);
        fields.put(asPair(scope, name), value);
    }

    public void registerProcessors(String name, List<PostProcessorConfiguration> postProcessors)
    {
        final OutputSpec spec = registeredOutputs.get(name);
        if (spec != null)
        {
            spec.processors.addAll(postProcessors);
        }
    }

    public void processOutputs()
    {
        for (OutputSpec spec: registeredOutputs.values())
        {
            final List<PostProcessor> processors = CollectionUtils.map(spec.processors, new Mapping<PostProcessorConfiguration, PostProcessor>()
            {
                public PostProcessor map(PostProcessorConfiguration postProcessorConfiguration)
                {
                    return postProcessorFactory.create(postProcessorConfiguration);
                }
            });

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(spec.toDir);
            scanner.setExcludes(null);
            scanner.scan();

            String prefix = spec.artifact.getName() + "/";
            for (String path : scanner.getIncludedFiles())
            {
                StoredFileArtifact fileArtifact = new StoredFileArtifact(prefix + path, spec.type);
                spec.artifact.add(fileArtifact);
                PostProcessorContext ppContext = new DefaultPostProcessorContext(fileArtifact, result, executionContext);
                for (PostProcessor pp: processors)
                {
                    pp.process(new File(spec.toDir, path), ppContext);
                }
            }            
        }
    }

    private static class OutputSpec
    {
        private StoredArtifact artifact;
        private String type;
        private File toDir;
        private List<PostProcessorConfiguration> processors = new LinkedList<PostProcessorConfiguration>();

        private OutputSpec(StoredArtifact artifact, String type, File toDir)
        {
            this.artifact = artifact;
            this.type = type;
            this.toDir = toDir;
        }
    }
}
