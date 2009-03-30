package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.List;
import java.util.LinkedList;

/**
 * The manually configured dependencies.
 */
@SymbolicName("zutubi.dependenciesConfiguration")
public class DependenciesConfiguration extends AbstractConfiguration
{
    private List<DependencyConfiguration> dependencies = new LinkedList<DependencyConfiguration>();

    private String publicationPattern = "build/[artifact].[ext]";
    
    private String retrievalPattern = "lib/[artifact].[ext]";

    private List<PublicationConfiguration> publications = new LinkedList<PublicationConfiguration>();

    public List<DependencyConfiguration> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(List<DependencyConfiguration> dependencies)
    {
        this.dependencies = dependencies;
    }

    public String getPublicationPattern()
    {
        return publicationPattern;
    }

    public void setPublicationPattern(String publicationPattern)
    {
        this.publicationPattern = publicationPattern;
    }

    public String getRetrievalPattern()
    {
        return retrievalPattern;
    }

    public void setRetrievalPattern(String retrievalPattern)
    {
        this.retrievalPattern = retrievalPattern;
    }

    public List<PublicationConfiguration> getPublications()
    {
        return publications;
    }

    public void setPublications(List<PublicationConfiguration> publications)
    {
        this.publications = publications;
    }
}
