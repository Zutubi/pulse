package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.config.docs.TypeDocs;

/**
 * Model of documentation for a type.
 */
public class DocModel
{
    private String brief;
    private String verbose;

    public DocModel(TypeDocs docs)
    {
        brief = docs.getBrief();
        verbose = docs.getVerbose();
    }

    public String getBrief()
    {
        return brief;
    }

    public String getVerbose()
    {
        return verbose;
    }
}
