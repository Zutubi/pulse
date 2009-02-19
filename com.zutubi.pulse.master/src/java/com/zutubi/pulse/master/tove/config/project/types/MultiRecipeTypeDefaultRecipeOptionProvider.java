package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Option provider that lists all available recipes in a
 * {@link com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration}.
 */
public class MultiRecipeTypeDefaultRecipeOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        List<String> recipes = new LinkedList<String>();
        if (instance != null)
        {
            MultiRecipeTypeConfiguration config = (MultiRecipeTypeConfiguration) instance;
            recipes.addAll(config.getRecipes().keySet());
            Collections.sort(recipes, new Sort.StringComparator());
        }

        return recipes;
    }
}
