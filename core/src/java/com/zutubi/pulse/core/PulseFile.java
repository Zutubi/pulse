package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class PulseFile implements ScopeAware
{
    /**
     * The name of the default recipe in this pulse file. 
     */
    private String defaultRecipe;

    /**
     * The name associated with this pulse file instance. The name is used for reporting purposes
     * only.
     */
    private String name;

    /**
     * The list of recipes defined within this pulse file.
     */
    private List<Recipe> recipes = new LinkedList<Recipe>();

/*
    Pulse file level dependencies are not yet implemented at the higher level.
    private List<Dependency> dependencies = new LinkedList<Dependency>();
*/

    /**
     * The global/root scope of this pulse file. 
     */
    private Scope globalScope;

    /**
     * Getter for the default recipe property.
     *
     * @return the default recipe property value.
     * 
     * @see PulseFile#defaultRecipe
     */
    public String getDefaultRecipe()
    {
        return defaultRecipe;
    }

    /**
     * Setter for the default recipe property.
     *
     * @param defaultRecipe the new default recipe property value
     *
     * @see PulseFile#defaultRecipe
     */
    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }

    public void addRecipe(Recipe r)
    {
        recipes.add(r);
    }

    public List<Recipe> getRecipes()
    {
        return Collections.unmodifiableList(recipes);
    }

    public Recipe getRecipe(String name)
    {
        if (!TextUtils.stringSet(name))
        {
            return null;
        }

        for (Recipe recipe : recipes)
        {
            if (name.equals(recipe.getName()))
            {
                return recipe;
            }
        }
        return null;
    }

/*
    REMOVED: PulseFile level dependencies are currently not implemented at the higher level.
    public List<Dependency> getDependencies()
    {
        return Collections.unmodifiableList(dependencies);
    }

    public void add(Dependency dependency)
    {
        dependencies.add(dependency);
    }
*/

    /**
     * The getter for the name property.
     *
     * @return the name associated with this pulse file instance.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The setter for the name property.
     *
     * @param name associated with this pulse file instance.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Getter for the scope associated with the pulse file. This is the global/root level scope.
     *
     * @return global/root scope
     */
    public Scope getScope()
    {
        return globalScope;
    }

    /**
     * Setter for the scope property.
     *
     * @param scope is the global/root scope associated with this pulse file.
     */
    public void setScope(Scope scope)
    {
        this.globalScope = scope;
    }
}
