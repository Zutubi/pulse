package com.zutubi.pulse.master.charting.build;

import com.zutubi.pulse.core.model.RecipeCustomFields;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource}
 * that loads the actual custom field values for recipes.  It also caches
 * across multiple uses.
 */
public class DefaultCustomFieldSource implements CustomFieldSource
{
    private File dataRoot;
    private Map<RecipeResult, RecipeCustomFields> resultToFields = new HashMap<RecipeResult, RecipeCustomFields>();

    public DefaultCustomFieldSource(File dataRoot)
    {
        this.dataRoot = dataRoot;
    }

    public String getFieldValue(RecipeResult recipeResult, String name)
    {
        RecipeCustomFields customFields = resultToFields.get(recipeResult);
        if (customFields == null)
        {
            customFields = new RecipeCustomFields(recipeResult.getAbsoluteOutputDir(dataRoot));
            resultToFields.put(recipeResult, customFields);
        }

        return customFields.load().get(name);
    }
}
