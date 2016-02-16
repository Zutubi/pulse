package com.zutubi.tove.ui.handler;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Abstract base for the common case of providing a map of options with keys
 * and values that are strings.  Deriving classes provide the map, this class
 * does the rest.
 */
public abstract class MapOptionProvider implements OptionProvider
{
    public abstract Option getEmptyOption(TypeProperty property, FormContext context);

    public List<Option> getOptions(TypeProperty property, FormContext context)
    {
        Map<String, String> optionMap = getMap(property, context);
        List<Option> options = Lists.newArrayList(Iterables.transform(optionMap.entrySet(), new Function<Map.Entry<String, String>, Option>()
        {
            @Override
            public Option apply(Map.Entry<String, String> input)
            {
                return new Option(input.getKey(), input.getValue());
            }
        }));

        sort(options);
        return options;
    }

    protected void sort(List<Option> options)
    {
        final Comparator<String> comparator = new Sort.StringComparator();
        Collections.sort(options, new Comparator<Option>()
        {
            @Override
            public int compare(Option o1, Option o2)
            {
                return comparator.compare(o1.getText(), o2.getText());
            }
        });
    }

    protected abstract Map<String, String> getMap(TypeProperty property, FormContext context);

    public String getOptionValue()
    {
        return "value";
    }

    public String getOptionText()
    {
        return "text";
    }

    public static class Option
    {
        private String value;
        private String text;

        public Option(String value, String text)
        {
            this.value = value;
            this.text = text;
        }

        public String getValue()
        {
            return value;
        }

        public String getText()
        {
            return text;
        }
    }
}
