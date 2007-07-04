package com.zutubi.pulse.prototype.config.project.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
@SymbolicName("zutubi.artifactConfig")
public class ArtifactConfiguration extends AbstractNamedConfiguration
{
    List<String> postprocessors = new LinkedList<String>();

    public List<String> getPostprocessors()
    {
        return postprocessors;
    }

    public void setPostprocessors(List<String> postprocessors)
    {
        this.postprocessors = postprocessors;
    }
}
