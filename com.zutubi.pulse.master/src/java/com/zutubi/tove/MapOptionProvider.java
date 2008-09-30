package com.zutubi.tove;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * Abstract base for the common case of providing a map of options with keys
 * and values that are strings.  Deriving classes provide the map, this class
 * does the rest.
 */
public abstract class MapOptionProvider implements OptionProvider
{
    public abstract MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property);

    public List<Map.Entry<String, String>> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        Map<String, String> optionMap = getMap(instance, parentPath, property);
        List<Map.Entry<String, String>> options = new ArrayList<Map.Entry<String, String>>(optionMap.entrySet());
        sort(options);
        return options;
    }

    protected void sort(List<Map.Entry<String, String>> options)
    {
        // By default, lexically sort by the values (what is displayed)
        final Comparator<String> comparator = new Sort.StringComparator();
        Collections.sort(options, new Comparator<Map.Entry<String, String>>()
        {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
            {
                return comparator.compare(o1.getValue(), o2.getValue());
            }
        });
    }

    protected abstract Map<String, String> getMap(Object instance, String parentPath, TypeProperty property);

    public String getOptionKey()
    {
        return "key";
    }

    public String getOptionValue()
    {
        return "value";
    }
}
