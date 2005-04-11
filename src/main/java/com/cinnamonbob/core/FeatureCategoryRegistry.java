package com.cinnamonbob.core;

import java.util.Map;
import java.util.TreeMap;

/**
 * Contains information about all categories of features.
 * 
 * @author jsankey
 */
public class FeatureCategoryRegistry
{
    /**
     * Mapping from category name to category.
     */
    private Map<String, FeatureCategory> categories;
    
    
    public FeatureCategoryRegistry()
    {
        categories = new TreeMap<String, FeatureCategory>();
        
        // Create the pre-defined categories
        addCategory(new FeatureCategory("error", FeatureCategory.Level.ERROR, "Errors"));
        addCategory(new FeatureCategory("warning", FeatureCategory.Level.WARNING, "Warnings"));
        addCategory(new FeatureCategory("info", FeatureCategory.Level.INFO, "Information"));
    }


    public boolean addCategory(FeatureCategory category)
    {
        if(categories.containsKey(category.getName()))
        {
            return false;
        }
        else
        {
            categories.put(category.getName(), category);
            return true;
        }
    }
    
    
    public boolean hasCategory(String name)
    {
        return categories.containsKey(name);
    }
    
    
    public FeatureCategory getFeatureCategory(String name)
    {
        return categories.get(name);
    }
}
