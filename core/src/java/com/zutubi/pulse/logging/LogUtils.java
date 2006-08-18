package com.zutubi.pulse.logging;

import java.util.Properties;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Filter;
import java.util.logging.Formatter;

/**
 * <class-comment/>
 */
class LogUtils
{
    protected static String getString(Properties config, String key, String defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return config.getProperty(key);
    }

    protected static int getInt(Properties config, String key, int defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return Integer.parseInt(config.getProperty(key));
    }

    protected static boolean getBoolean(Properties config, String key, boolean defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(config.getProperty(key));
    }

    protected static Level getLevel(Properties config, String key, Level defaultValue)
    {
        if (!config.containsKey(key))
        {
            return defaultValue;
        }
        return Level.parse(config.getProperty(key));
    }

    protected static Filter getFilter(Properties config, String key, Filter defaultValue)
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

    protected static Formatter getFormatter(Properties config, String key, Formatter defaultValue)
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
            return (Formatter) clz.newInstance();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    protected static List<String> getList(Properties config, String key, List<String> defaultValue)
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
