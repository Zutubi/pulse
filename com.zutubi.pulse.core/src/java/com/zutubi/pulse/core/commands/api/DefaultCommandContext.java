package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class DefaultCommandContext implements CommandContext
{
    private ExecutionContext executionContext;
    private CommandResult result;
    private Map<String, OutputSpec> registeredOutputs = new LinkedHashMap<String, OutputSpec>();

    public DefaultCommandContext(ExecutionContext executionContext, CommandResult result)
    {
        this.executionContext = executionContext;
        this.result = result;
    }

    public void addArtifacts()
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

    public void processOutput(String name, List<PostProcessorConfiguration> postProcessors)
    {
        final OutputSpec spec = registeredOutputs.get(name);
        if (spec == null)
        {
            // Nothing to process.
            return;
        }

        final List<PostProcessor> processors = CollectionUtils.map(postProcessors, new Mapping<PostProcessorConfiguration, PostProcessor>()
        {
            public PostProcessor map(PostProcessorConfiguration postProcessorConfiguration)
            {
                return postProcessorConfiguration.createProcessor();
            }
        });

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(spec.toDir);
        scanner.setExcludes(null);
        scanner.scan();

        for (String path : scanner.getIncludedFiles())
        {
            StoredFileArtifact fileArtifact = new StoredFileArtifact(path, spec.type);
            spec.artifact.add(fileArtifact);
            PostProcessorContext ppContext = new DefaultPostProcessorContext(fileArtifact, result, executionContext);
            for (PostProcessor pp: processors)
            {
                pp.process(new File(spec.toDir, path), ppContext);
            }
        }
    }

    private static class OutputSpec
    {
        private StoredArtifact artifact;
        private String type;
        private File toDir;

        private OutputSpec(StoredArtifact artifact, String type, File toDir)
        {
            this.artifact = artifact;
            this.type = type;
            this.toDir = toDir;
        }
    }
}
