package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 * An interface to abstract the details of loading custom fields for recipe results.
 */
public interface CustomFieldSource
{
    /**
     * Retrieves the value of a given custom field for the given recipe result,
     * if it exists.
     *
     * @param recipeResult the recipe to get the field value for
     * @param name         the name of the field to retrieve
     * @return the field value, or null if it has no value
     */
    String getFieldValue(RecipeResult recipeResult, String name);
}
