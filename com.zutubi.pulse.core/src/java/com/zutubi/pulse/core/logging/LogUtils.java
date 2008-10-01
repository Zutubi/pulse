package com.zutubi.pulse.core.logging;

import com.zutubi.util.ClassLoaderUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * <class-comment/>
 */
public class LogUtils
{
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
        return Level.parse(config.getProperty(key));
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
