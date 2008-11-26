package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.util.List;

/**
 * <class comment/>
 */
public abstract class ArtifactSupport implements Artifact
{
    /**
     * The name of this artifact allows it to be referenced by name.
     */
    private String name;

    /**
     * Get the name of this artifact.
     *
     * @return the name of the artifact.
     */
    @Required
    @Constraint("ArtifactNameValidator")
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of this artifact.
     *
     * @param name of this artifact.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    protected void processArtifact(StoredFileArtifact fileArtifact, CommandResult result, ExecutionContext context, List<ProcessArtifact> processes)
    {
        File outputDir = context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR);
        for (ProcessArtifact process : processes)
        {
            PostProcessor postProcessor = process.getProcessor();
            PostProcessorContext ppContext = new DefaultPostProcessorContext(fileArtifact, result, context);
            postProcessor.process(new File(outputDir, fileArtifact.getPath()), ppContext);
        }
    }
}
