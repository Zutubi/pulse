package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.ListOptionProvider;
import com.zutubi.util.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Option provider that lists all available recipes in a
 * {@link com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration}.
 */
public class MultiRecipeTypeDefaultRecipeOptionProvider extends ListOptionProvider
{
    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        List<String> recipes = new ArrayList<>();
        if (context.getExistingInstance() != null)
        {
            MultiRecipeTypeConfiguration config = (MultiRecipeTypeConfiguration) context.getExistingInstance();
            recipes.addAll(config.getRecipes().keySet());
            Collections.sort(recipes, new Sort.StringComparator());
        }

        return recipes;
    }
}
