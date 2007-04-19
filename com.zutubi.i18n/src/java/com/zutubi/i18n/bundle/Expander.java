package com.zutubi.i18n.bundle;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <class-comment/>
 */
public class Expander
{
    public List<String> expand(String name, Locale locale, String suffix)
    {
        List<String> names = new ArrayList<String>();

        StringBuffer buffer = new StringBuffer(name);
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        names.add(name + suffix);

        if (! "".equals(language))
        {
            buffer.append("_");
            buffer.append(language);
            names.add(buffer.toString() + suffix);
        }

        if (! "".equals(country))
        {
            buffer.append("_");
            buffer.append(country);
            names.add(buffer.toString() + suffix);
        }

        if (! "".equals(variant))
        {
            buffer.append("_");
            buffer.append(variant);
            names.add(buffer.toString() + suffix);
        }

        Collections.reverse(names);
        return names;
    }

}