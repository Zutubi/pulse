package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
@SymbolicName("zutubi.artifactConfig")
@Table(columns = {"name", "details"})
public abstract class ArtifactConfiguration extends AbstractNamedConfiguration
{
    @Select(optionProvider = "PostProcessorOptionProvider")
    List<String> postprocessors = new LinkedList<String>();

    public ArtifactConfiguration()
    {
    }

    public ArtifactConfiguration(String name)
    {
        super(name);
    }

    public List<String> getPostprocessors()
    {
        return postprocessors;
    }

    public void setPostprocessors(List<String> postprocessors)
    {
        this.postprocessors = postprocessors;
    }

    public void addPostprocessor(String postprocessor)
    {
        postprocessors.add(postprocessor);
    }

    @Transient
    public abstract String getType();
}
