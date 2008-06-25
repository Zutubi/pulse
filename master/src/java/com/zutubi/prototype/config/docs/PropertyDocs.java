package com.zutubi.prototype.config.docs;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds documentation for a single field of a type.  Only simple fields are
 * documented: complex/nested fields are represented by their own
 * {@link TypeDocs}.
 */
public class PropertyDocs implements Docs
{
    private String name;
    private String type;
    private String label;
    private String brief;
    private String verbose;
    private List<Example> examples = new LinkedList<Example>();

    public PropertyDocs(String name, String type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getBrief()
    {
        return brief;
    }

    public void setBrief(String brief)
    {
        this.brief = brief;
    }

    public String getVerbose()
    {
        return verbose;
    }

    public void setVerbose(String verbose)
    {
        this.verbose = verbose;
    }

    public List<Example> getExamples()
    {
        return examples;
    }

    public void addExample(Example example)
    {
        examples.add(example);
    }
}
