package com.zutubi.pulse.core.commands;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.*;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.SecurityUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.logging.Logger;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_CUSTOM_FIELDS;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Default implementation of {@link com.zutubi.pulse.core.commands.api.CommandContext}.
 */
public class DefaultCommandContext implements CommandContext
{
    private static final Logger LOG = Logger.getLogger(DefaultCommandContext.class);
    private static final Messages I18N = Messages.getInstance(DefaultCommandContext.class);
    
    private ExecutionContext executionContext;
    private CommandResult result;
    private int perFileFeatureLimit;
    private Map<String, ArtifactSpec> registeredArtifacts = new LinkedHashMap<String, ArtifactSpec>();
    private PostProcessorFactory postProcessorFactory;

    public DefaultCommandContext(ExecutionContext executionContext, CommandResult result, int perFileFeatureLimit, PostProcessorFactory postProcessorFactory)
    {
        this.executionContext = executionContext;
        this.result = result;
        this.perFileFeatureLimit = perFileFeatureLimit;
        this.postProcessorFactory = postProcessorFactory;
    }

    /**
     * It is important that {@link #processArtifacts()} is called before this method
     * to ensure that artifact files have been both picked up and processed before
     * recording them on the result. 
     */
    public void addArtifactsToResult()
    {
        for (ArtifactSpec spec: registeredArtifacts.values())
        {
            if (spec.getArtifact().getChildren().size() > 0)
            {
                result.addArtifact(spec.getArtifact());
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

    public void warning(String message)
    {
        result.warning(message);
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

    public void registerLink(String name, String url, boolean explicit, boolean featured)
    {
        result.addArtifact(new StoredArtifact(name, url, explicit, featured));
    }

    public File registerArtifact(String name, String type, boolean explicit, boolean featured, HashAlgorithm hashAlgorithm)
    {
        StoredArtifact artifact = new StoredArtifact(name, explicit, featured);
        File toDir = new File(executionContext.getFile(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_OUTPUT_DIR), name);
        if (!toDir.mkdirs())
        {
            throw new BuildException("Unable to create storage directory '" + toDir.getAbsolutePath() + "' for artifact '" + name + "'");
        }

        registeredArtifacts.put(name, new ArtifactSpec(artifact, type, toDir, hashAlgorithm));
        return toDir;
    }

    public void setArtifactIndex(String name, String index)
    {
        ArtifactSpec spec = registeredArtifacts.get(name);
        if (spec == null)
        {
            throw new BuildException("Attempt to set index file for unknown artifact '" + name + "'");
        }

        spec.getArtifact().setIndex(index);
    }

    public void markArtifactForPublish(String name, String pattern)
    {
        ArtifactSpec spec = registeredArtifacts.get(name);
        if (spec == null)
        {
            throw new BuildException("Attempt to set publish for unknown artifact '" + name + "'");
        }
        spec.getArtifact().setPublish(true);
        spec.getArtifact().setArtifactPattern(pattern);
    }

    public void addCustomField(FieldScope scope, String name, String value)
    {
        if (!StringUtils.stringSet(name))
        {
            throw new IllegalArgumentException("Name must be specified");
        }

        @SuppressWarnings({"unchecked"})
        Map<Pair<FieldScope, String>, String> fields = executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_CUSTOM_FIELDS, Map.class);
        fields.put(asPair(scope, name), value);
    }

    public void registerProcessors(String name, List<PostProcessorConfiguration> postProcessors)
    {
        ArtifactSpec spec = registeredArtifacts.get(name);
        if (spec == null)
        {
            throw new BuildException("Attempt to register processors for unknown artifact '" + name + "'");
        }
        spec.addAll(postProcessors);
    }

    public void processArtifacts()
    {
        for (ArtifactSpec spec: registeredArtifacts.values())
        {
            final List<PostProcessor> processors = CollectionUtils.map(spec.getProcessors(), new Mapping<PostProcessorConfiguration, PostProcessor>()
            {
                public PostProcessor map(PostProcessorConfiguration postProcessorConfiguration)
                {
                    return postProcessorFactory.create(postProcessorConfiguration);
                }
            });

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(spec.getToDir());
            scanner.setExcludes(null);
            scanner.scan();

            String prefix = spec.getArtifact().getName() + "/";
            for (String path : scanner.getIncludedFiles())
            {
                StoredFileArtifact fileArtifact = new StoredFileArtifact(prefix + path, spec.getType());
                File file = new File(spec.getToDir(), path);

                HashAlgorithm hashAlgorithm = spec.getHashAlgorithm();
                if (hashAlgorithm != null)
                {
                    calculateHash(fileArtifact, file, hashAlgorithm);
                }

                spec.getArtifact().add(fileArtifact);
                DefaultPostProcessorContext ppContext = new DefaultPostProcessorContext(fileArtifact, result, perFileFeatureLimit, executionContext);
                for (PostProcessor pp: processors)
                {
                    pp.process(file, ppContext);
                }
                
                if (perFileFeatureLimit > 0 && ppContext.isFeaturesDiscarded())
                {
                    fileArtifact.addFeature(new PersistentFeature(Feature.Level.INFO, I18N.format("feature.limit.reached")));
                }
            }            
        }
    }

    private void calculateHash(StoredFileArtifact fileArtifact, File file, HashAlgorithm hashAlgorithm)
    {
        try
        {
            fileArtifact.setHash(SecurityUtils.digest(hashAlgorithm.getDigestName(), file));
        }
        catch (NoSuchAlgorithmException e)
        {
            LOG.warning(e);
        }
        catch (IOException e)
        {
            LOG.warning("I/O error calculating hash for file '" + file.getAbsolutePath() + "' " + e.getMessage(), e);
        }
    }

    private static class ArtifactSpec
    {
        private StoredArtifact artifact;
        private String type;
        private File toDir;
        private HashAlgorithm hashAlgorithm;
        private List<PostProcessorConfiguration> processors = new LinkedList<PostProcessorConfiguration>();

        private ArtifactSpec(StoredArtifact artifact, String type, File toDir, HashAlgorithm hashAlgorithm)
        {
            this.artifact = artifact;
            this.type = type;
            this.toDir = toDir;
            this.hashAlgorithm = hashAlgorithm;
        }

        public void addAll(List<PostProcessorConfiguration> postProcessors)
        {
            this.getProcessors().addAll(postProcessors);
        }

        public StoredArtifact getArtifact()
        {
            return artifact;
        }

        public String getType()
        {
            return type;
        }

        public File getToDir()
        {
            return toDir;
        }

        public HashAlgorithm getHashAlgorithm()
        {
            return hashAlgorithm;
        }

        public List<PostProcessorConfiguration> getProcessors()
        {
            return processors;
        }
    }
}
