package com.zutubi.pulse.core.model;

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Manages custom fields related to a recipe result.
 */
public class RecipeCustomFields
{
    public static final String CUSTOM_FIELDS_FILE = "fields.properties";

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
            // Use a tree map to sort properties.
            customFields = new TreeMap<String, String>();
            File customFile = new File(recipeOutputDir, CUSTOM_FIELDS_FILE);
            if (customFile.exists())
            {
                FileInputStream inputStream = null;
                try
                {
                    inputStream = new FileInputStream(customFile);
                    Properties p = new Properties();
                    p.load(inputStream);
                    for (Map.Entry<Object, Object> entry: p.entrySet())
                    {
                        customFields.put(entry.getKey().toString(), entry.getValue().toString());
                    }

                }
                catch (IOException e)
                {
                    LOG.severe("Unable to load custom fields: " + e.getMessage(), e);
                }
                finally
                {
                    IOUtils.close(inputStream);
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
        // Clear cached fields.
        this.customFields = null;

        Properties properties = new Properties();
        properties.putAll(customFields);
        File customFile = new File(recipeOutputDir, CUSTOM_FIELDS_FILE);
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(customFile);
            properties.store(outputStream, "Custom Fields");
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write out custom fields to file '" + customFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(outputStream);
        }
    }
}
