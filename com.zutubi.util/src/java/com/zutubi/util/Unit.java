package com.zutubi.util;

/**
 * Represents a unit of time.
 */
public enum Unit
{
    DAY
    {
        public long toMilliseconds()
        {
            return Constants.DAY;
        }
    },
    HOUR
    {
        public long toMilliseconds()
        {
            return Constants.HOUR;
        }
    },
    MINUTE
    {
        public long toMilliseconds()
        {
            return Constants.MINUTE;
        }
    },
    SECOND
    {
        public long toMilliseconds()
        {
            return Constants.SECOND;
        }
    },
    MILLISECOND
    {
        public long toMilliseconds()
        {
            return Constants.MILLISECOND;
        }
    };

    /**
     * @return the number of milliseconds that this unit of time represents.
     */
    public abstract long toMilliseconds();

    /**
     * Determines the <code>Unit</code> value of the system property
     * with the specified name.
     * <p/>
     * The key argument is the name of a system property that will be
     * interpreted as a <code>Unit</code> value.
     * <p/>
     * If there is no property with the specified name, if the specified
     * name is empty or <code>null</code>, or if the property does not
     * have the correct format, then <code>null</code> is returned.
     * <p/>
     * In other words, this method returns a <code>Unit</code> object equal to
     * the value of:
     * <blockquote><code>
     * getUnit(key, null)
     * </code></blockquote>
     *
     * @param key the system property name
     * @return the interpreted Unit value, or null.
     */
    public static Unit getUnit(String key)
    {
        return getUnit(key, null);
    }

    /**
     * Determines the <code>Unit</code> value of the system property
     * with the specified name.
     * <p/>
     * The key argument is the name of a system property that will be
     * interpreted as a <code>Unit</code> value.
     * <p/>
     * If there is no property with the specified name, if the specified
     * name is empty or <code>null</code>, or if the property does not
     * have the correct format, then the default value is returned.
     * <p/>
     *
     * @param key         the system property name
     * @param defaultUnit the value returned if no value can be
     *                    interpreted from the given key.
     * @return the interpreted Unit value, or the default value.
     */
    public static Unit getUnit(String key, Unit defaultUnit)
    {
        return parse(System.getProperty(key), defaultUnit);
    }

    /**
     * Parse the given string value, returning the equivalent unit object.
     * <p/>
     * Valid values are those that are a case insensitive match to the Unit
     * enumerations string representations.
     *
     * @param str string to be parsed.
     * @return a Unit object equivalent to the string.
     * @see #MILLISECOND
     * @see #MINUTE
     * @see #SECOND
     * @see #HOUR
     * @see #DAY
     */
    public static Unit parse(String str)
    {
        if (str == null)
        {
            throw new IllegalArgumentException("<null> value specified.");
        }
        return Unit.valueOf(str.toUpperCase());
    }

    /**
     * Parse the given string value, returning the equivalent unit object.
     * <p/>
     * If the string value for some reason can not be interpreted into a Unit
     * object, the specified default value will be returned.
     *
     * @param str         string to be parsed.
     * @param defaultUnit the value returned if the str value can not be
     *                    interpreted into a Unit value.
     * @return a Unit object equivalent to the string.
     * @see #MILLISECOND
     * @see #MINUTE
     * @see #SECOND
     * @see #HOUR
     * @see #DAY
     */
    public static Unit parse(String str, Unit defaultUnit)
    {
        try
        {
            return Unit.parse(str);
        }
        catch (IllegalArgumentException e)
        {
            return defaultUnit;
        }
    }
}
