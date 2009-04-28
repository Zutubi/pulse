package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * A publication defined a build artifact that is to be published to the internal
 * artifact repository.  These artifacts can later be retrieved and used by other builds
 * via build dependencies.
 */
@SymbolicName("zutubi.publication")
@Form(fieldOrder={"name", "ext"})
@Table(columns = {"name", "ext"})
public class PublicationConfiguration extends AbstractConfiguration
{
    @Required
    private String name;

    @Required
    private String ext = "jar";

    public PublicationConfiguration()
    {
    }

    public PublicationConfiguration(String name, String ext)
    {
        this.name = name;
        this.ext = ext;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getExt()
    {
        return ext;
    }

    public void setExt(String ext)
    {
        this.ext = ext;
    }
}
