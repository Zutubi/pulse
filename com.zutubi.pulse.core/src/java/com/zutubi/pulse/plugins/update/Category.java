package com.zutubi.pulse.plugins.update;

import java.util.LinkedList;
import java.util.List;

/**
 * A category is a grouping of features on an update site, used purely for
 * organisational purposes.
 */
public class Category
{
    /**
     * The category name is a unique identifier for the category, used by
     * features to reference the category.
     */
    private String name;
    /**
     * The label is a human-readable identifier for the category.  It is
     * displayed in the UI.
     */
    private String label;
    /**
     * An optional description for the category (may be null).
     */
    private String description;
    /**
     * Features contained within this category (note that features may be in
     * multiple categories.
     */
    private List<FeatureReference> featureReferences = new LinkedList<FeatureReference>();


    public Category(String name, String label, String description)
    {
        this.name = name;
        this.label = label;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public List<FeatureReference> getFeatureReferences()
    {
        return featureReferences;
    }

    public void addFeatureReference(FeatureReference featureReference)
    {
        featureReferences.add(featureReference);
        featureReference.addCategory(this);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Category:\n");
        sb.append("  name       : ").append(name).append('\n');
        sb.append("  label      : ").append(label).append('\n');
        sb.append("  description: ").append(description).append('\n');
        sb.append("  features   : ").append('\n');
        for(FeatureReference featureReference: featureReferences)
        {
            sb.append(featureReference).append('\n');
        }
        sb.append("/Category");

        return sb.toString();
    }
}
