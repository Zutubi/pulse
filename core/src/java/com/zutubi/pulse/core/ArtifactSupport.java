package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

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

    protected void processArtifact(StoredFileArtifact fileArtifact, CommandResult result, CommandContext context, List<ProcessArtifact> processes)
    {
        for (ProcessArtifact process : processes)
        {
            process.getProcessor().process(fileArtifact, result, context);
        }
    }
}
