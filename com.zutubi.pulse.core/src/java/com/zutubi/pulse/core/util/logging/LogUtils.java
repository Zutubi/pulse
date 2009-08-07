package com.zutubi.pulse.core.util.logging;

import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * A set of convenience methods that simplify access to the logging properties through parsing
 * the strings and loading classes.
 */
public class LogUtils
{
    private static final List<Level> predefined = new LinkedList<Level>();

    static
    {
        predefined.add(Level.ALL);
        predefined.add(Level.CONFIG);
        predefined.add(Level.FINE);
        predefined.add(Level.FINER);
        predefined.add(Level.FINEST);
        predefined.add(Level.INFO);
        predefined.add(Level.OFF);
        predefined.add(Level.SEVERE);
        predefined.add(Level.WARNING);
    }

    public static String getString(Properties config, String key, String defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return config.getProperty(key);
    }

    public static int getInt(Properties config, String key, int defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return Integer.parseInt(config.getProperty(key));
    }

    public static boolean getBoolean(Properties config, String key, boolean defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(config.getProperty(key));
    }

    public static Level getLevel(Properties config, String key, Level defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        try
        {
            return Level.parse(config.getProperty(key));
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Failed to parse log level for '" + key + "' using default Level.parse().  " +
                    "Reverting to custom parse.");

            // CIB-766.  Still have not been able to determine what the root cause of this is.
            String value = config.getProperty(key);
            if (!StringUtils.stringSet(value))
            {
                return defaultValue;
            }

            value = value.trim();
            for (Level l : predefined)
            {
                if (value.compareToIgnoreCase(l.getName()) == 0)
                {
                    return l;
                }
            }

            System.err.println("Failed to parse log level for '" + key + "', value '" + value + "'. " + e.getMessage());
            return defaultValue;
        }
    }

    public static Filter getFilter(Properties config, String key, Filter defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }

        String clsName = config.getProperty(key);
        if (clsName == null)
        {
            return defaultValue;
        }
        try
        {
            Class clz = ClassLoader.getSystemClassLoader().loadClass(clsName);
            return (Filter) clz.newInstance();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    public static Formatter getFormatter(Properties config, String key, Formatter defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }

        String clsName = config.getProperty(key);
        if (clsName == null)
        {
            return defaultValue;
        }
        try
        {
            Class clz = ClassLoaderUtils.loadClass(clsName, config.getClass());
            return (Formatter) clz.newInstance();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * This method looks up the property associated with the specified key in the properties object, and
     * treats it as a comma separated list of values.  These values are returned in a list.  If the key
     * is not found in the properties, or does not refer to a value, the defaultValue is returned.
     *
     * @param config       is the properties instance from which the list of values is retrieved.
     * @param key          is the name of the property being retrieved.
     * @param defaultValue is the default value, returned if no suitable value is value can be retrieved.
     * @return a list of strings, representing the individual values of the comma separated list.
     */
    public static List<String> getList(Properties config, String key, List<String> defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        String str = config.getProperty(key);
        if (str == null)
        {
            return defaultValue;
        }

        List<String> list = new LinkedList<String>();

        StringTokenizer tokenizer = new StringTokenizer(str, " ,", false);
        while (tokenizer.hasMoreTokens())
        {
            list.add(tokenizer.nextToken());
        }

        return list;
    }

}
