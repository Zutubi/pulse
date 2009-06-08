package com.zutubi.pulse.core.model;

import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages custom fields related to a recipe result.
 */
public class RecipeCustomFields
{
    public static final String CUSTOM_FIELDS_FILE = "custom.properties";

    private static final Logger LOG = Logger.getLogger(RecipeCustomFields.class);

    private File recipeOutputDir;
    /** Caches loaded fields. */
    private Map<String, String> customFields;

    public RecipeCustomFields(File recipeOutputDir)
    {
        this.recipeOutputDir = recipeOutputDir;
    }

    /**
     * Loads and returns all custom fields for a recipe from disk.  If no
     * custom fields file is found, or an error occurs, an empty map is
     * returned.
     *
     * @return a mapping of field name to field value for all custom fields for
     *         a recipe
     */
    public Map<String, String> load()
    {
        if (customFields == null)
        {
            customFields = new HashMap<String, String>();
            File customFile = new File(recipeOutputDir, CUSTOM_FIELDS_FILE);
            if (customFile.exists())
            {
                try
                {
                    Properties p = new Properties();
                    p.load(new FileReader(customFile));
                    for (Map.Entry<Object, Object> entry: p.entrySet())
                    {
                        customFields.put(entry.getKey().toString(), entry.getValue().toString());
                    }

                }
                catch (IOException e)
                {
                    LOG.severe("Unable to load custom fields: " + e.getMessage(), e);
                }
            }
        }

        return customFields;
    }

    /**
     * Stores the given custom fields to disk as part of a recipe result.
     *
     * @param customFields a mapping of field name to field value for all custom
     *                     fields
     */
    public void store(Map<String, String> customFields)
    {
        Properties properties = new Properties();
        properties.putAll(customFields);
        File customFile = new File(recipeOutputDir, CUSTOM_FIELDS_FILE);
        try
        {
            properties.store(new FileWriter(customFile), "Custom Fields");
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write out custom fields to file '" + customFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }
}
