package com.cinnamonbob.core2;

import java.util.*;

/**
 * 
 *
 */
public class Project
{
    /**
     * 
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * 
     */
    private final Map<String, Object> references = new HashMap<String, Object>();

    /**
     * 
     */
    private final List<Recipe> recipes = new LinkedList<Recipe>();

    /**
     * 
     */ 
    private final List<Schedule> schedules = new LinkedList<Schedule>();

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String description;

    /**
     * @param key
     * @return
     */
    public String getProperty(String key)
    {
        return properties.get(key);
    }

    /**
     * @param key
     * @param value
     */
    public void setProperty(String key, String value)
    {
        properties.put(key, value);
    }

    /**
     * Getter for the projects name property.
     *
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Setter for the projects name property
     *
     * @param name
     * @see #name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public List<Recipe> getRecipes()
    {
        return Collections.unmodifiableList(recipes);
    }

    public void addRecipe(Recipe r)
    {
        r.setProject(this);
        recipes.add(r);
    }

    /**
     * Getter for the projects description property
     *
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Setter for the projects description property
     *
     * @param description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Resolve all variable references.
     */
    protected void resolveAllProperties() throws BobException
    {
        //TODO: should check for circular references between properties.
        for (String name : properties.keySet())
        {
            String value = properties.get(name);
            String newValue = VariableHelper.replaceVariables(value, properties);
            properties.put(name, newValue);
        }
    }

    protected String resolveProperties(String input) throws BobException
    {
        return VariableHelper.replaceVariables(input, properties);
    }

    public Map getReferences()
    {
        return Collections.unmodifiableMap(references);
    }

    public Object getReference(String key)
    {
        return references.get(key);
    }
    
    public void addReference(String key, Object value)
    {
        references.put(key, value);
    }

    public void addSchedule(Schedule schedule)
    {
        schedules.add(schedule);
    }
}
