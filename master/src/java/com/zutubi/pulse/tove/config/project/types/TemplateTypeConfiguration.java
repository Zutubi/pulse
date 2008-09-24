package com.zutubi.pulse.tove.config.project.types;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.PostProcessorManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

/**
 *
 *
 */
@Wire
@SymbolicName("zutubi.templateTypeConfig")
public abstract class TemplateTypeConfiguration extends TypeConfiguration
{
    private static final Logger LOG = Logger.getLogger(TemplateTypeConfiguration.class);

    @Select(optionProvider = "PostProcessorOptionProvider")
    @Wizard.Ignore
    private List<String> postProcessors = new LinkedList<String>();

    private Map<String, ArtifactConfiguration> artifacts = new LinkedHashMap<String, ArtifactConfiguration>();

    @Transient
    private VelocityEngine velocityEngine;
    @Transient
    private PostProcessorManager postProcessorManager;

    public List<String> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<String> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(String postprocessor)
    {
        postProcessors.add(postprocessor);    
    }

    public Map<String, ArtifactConfiguration> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(Map<String, ArtifactConfiguration> artifacts)
    {
        this.artifacts = artifacts;
    }

    public void addArtifact(ArtifactConfiguration artifact)
    {
        artifacts.put(artifact.getName(), artifact);
    }

    public String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch)
    {
        try
        {
            VelocityContext context = new VelocityContext();
            addPostProcessors(context);
            context.put("details", this);
            context.put("outputProcessors", postProcessors);
            context.put("artifacts", artifacts);
            setupContext(context);
            StringWriter stringWriter = new StringWriter(1024);
            getVelocityEngine().mergeTemplate("pulse-file" + File.separatorChar + getTemplateName(), context, stringWriter);
            return stringWriter.getBuffer().toString();
        }
        catch (Exception e)
        {
            LOG.warning(e);
            throw new BuildException("Loading template pulse file: " + e.getMessage(), e);
        }
    }

    protected abstract String getTemplateName();

    protected abstract void setupContext(VelocityContext context);

    public String getReference(String name)
    {
        // Help out velocity, which appears to be completely retarded when it
        // comes to escaping.
        return "${" + name + "}";
    }

    public VelocityEngine getVelocityEngine()
    {
        if(velocityEngine == null)
        {
            velocityEngine = (VelocityEngine) SpringComponentContext.getBean("velocityEngine");
        }
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine)
    {
        this.velocityEngine = velocityEngine;
    }

    private void addPostProcessors(VelocityContext context)
    {
        Set<String> includedProcessors = new TreeSet<String>();
        List<String> fragments = new LinkedList<String>();

        for(String processor: postProcessors)
        {
            addProcessor(includedProcessors, processor, fragments);
        }

        for(ArtifactConfiguration artifact: artifacts.values())
        {
            for(String processor: artifact.getPostprocessors())
            {
                addProcessor(includedProcessors, processor, fragments);
            }
        }

        context.put("postProcessorFragments", fragments);
    }

    private void addProcessor(Set<String> includedProcessors, String processor, List<String> fragments)
    {
        PostProcessorFragment fragment = postProcessorManager.getProcessor(processor);
        if(fragment != null && includedProcessors.add(processor))
        {
            fragments.add(fragment.getFragment());
        }
    }

    public void setPostProcessorManager(PostProcessorManager postProcessorManager)
    {
        this.postProcessorManager = postProcessorManager;
    }
}
