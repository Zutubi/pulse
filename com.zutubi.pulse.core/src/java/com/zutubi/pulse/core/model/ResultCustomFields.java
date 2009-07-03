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
 * Manages custom fields related to a result.
 */
public class ResultCustomFields
{
    public static final String CUSTOM_FIELDS_FILE = "fields.properties";
    
    public static final String FIELD_VERSION = "version";

    private static final Logger LOG = Logger.getLogger(ResultCustomFields.class);

    private File outputDir;
    private String filename;
    /** Caches loaded fields. */
    private Map<String, String> customFields;

    public ResultCustomFields(File outputDir)
    {
        this(outputDir, CUSTOM_FIELDS_FILE);
    }

    public ResultCustomFields(File outputDir, String filename)
    {
        this.outputDir = outputDir;
        this.filename = filename;
    }

    /**
     * Loads and returns all custom fields for a result from disk.  If no
     * custom fields file is found, or an error occurs, an empty map is
     * returned.
     *
     * @return a mapping of field name to field value for all custom fields for
     *         a result
     */
    public Map<String, String> load()
    {
        if (customFields == null)
        {
            // Use a tree map to sort properties.
            customFields = new TreeMap<String, String>();
            File customFile = new File(outputDir, filename);
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
     * Stores the given custom fields to disk as part of a result.
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
        File customFile = new File(outputDir, filename);
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
