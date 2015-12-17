package com.zutubi.tove.config.docs;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds documentation for a single field of a type.  Only simple fields are
 * documented: complex/nested fields are represented by their own
 * {@link TypeDocs}.
 */
public class PropertyDocs implements Docs
{
    private String name;
    private String label;
    private String brief;
    private String verbose;
    private List<Example> examples;

    public PropertyDocs(String name)
    {
        this.name = name;
    }

    public PropertyDocs(String name, String label, String brief)
    {
        this.name = name;
        this.label = label;
        this.brief = brief;
    }

    public String getName()
    {
        return name;
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
        if (examples == null)
        {
            examples = new ArrayList<>();
        }

        examples.add(example);
    }
}
