package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.adt.Pair;

import java.util.*;

/**
 * Converts maps to and from strings.  All keys must be of the same, squeezable
 * class (likewise for values).  Maps are encode in the string as sequences of
 * entries separated by commas, where each entry is a key and value separated
 * by a colon.
 */
public class MapSqueezer implements TypeSqueezer
{
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char ENTRY_SEPARATOR = ',';
    
    private static final AllowedCharactersPredicate ALLOWED_CHARACTERS_PREDICATE = new AllowedCharactersPredicate();

    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        @SuppressWarnings({"unchecked"})
        Map<Object, Object> map = (Map) obj;
        TypeSqueezer keySqueezer = addClass(map.keySet(), builder);
        builder.append(KEY_VALUE_SEPARATOR);
        TypeSqueezer valueSqueezer = addClass(map.values(), builder);

        for (Map.Entry<Object, Object> entry: map.entrySet())
        {
            builder.append(ENTRY_SEPARATOR);
            builder.append(encode(keySqueezer.squeeze(entry.getKey())));
            builder.append(KEY_VALUE_SEPARATOR);
            builder.append(encode(valueSqueezer.squeeze(entry.getValue())));
        }
        
        return builder.toString();
    }

    private TypeSqueezer addClass(Collection<Object> collection, StringBuilder builder) throws SqueezeException
    {
        Class clazz = null;
        for (Object o : collection)
        {
            if (o != null)
            {
                if (clazz == null)
                {
                    clazz = o.getClass();
                }
                else if (!clazz.equals(o.getClass()))
                {
                    throw new SqueezeException("Unable to squeeze maps with different classes '" + clazz + "' and '" + o.getClass() + "'");
                }
            }
        }

        TypeSqueezer squeezer;
        if (clazz == null)
        {
            squeezer = new NullSqueezer();
        }
        else
        {
            builder.append(clazz.getName());
            squeezer = Squeezers.findSqueezer(clazz);
            if (squeezer == null)
            {
                throw new SqueezeException("Cannot convert: no squeezer for class '" + clazz + "'");
            }
        }

        return squeezer;
    }

    private String encode(String s)
    {
        return s == null ? null : WebUtils.percentEncode(s, ALLOWED_CHARACTERS_PREDICATE);
    }

    public Object unsqueeze(String s) throws SqueezeException
    {
        if (!StringUtils.stringSet(s))
        {
            return null;
        }
        
        List<Pair<String, String>> parts = split(s);
        if (parts.isEmpty())
        {
            throw new SqueezeException("Missing key and value types");
        }
        
        Pair<String, String> classPair = parts.get(0);
        TypeSqueezer keySqueezer = getSqueezer(classPair.first);
        TypeSqueezer valueSqueezer = getSqueezer(classPair.second);
        
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (Pair<String, String> part: parts.subList(1, parts.size()))
        {
            result.put(keySqueezer.unsqueeze(part.first), valueSqueezer.unsqueeze(part.second));
        }
        
        return result;
    }

    private TypeSqueezer getSqueezer(String className) throws SqueezeException
    {
        if (StringUtils.stringSet(className))
        {
            try
            {
                Class keyClass = Class.forName(className);
                TypeSqueezer squeezer = Squeezers.findSqueezer(keyClass);
                if (squeezer == null)
                {
                    throw new SqueezeException("Cannot convert: no squeezer for class '" + keyClass + "'");
                }
                
                return squeezer;
            }
            catch (ClassNotFoundException e)
            {
                throw new SqueezeException("Invalid key type '" + className + "')");
            }
        }
        else
        {
            return new NullSqueezer();
        }
    }

    private List<Pair<String, String>> split(String s) throws SqueezeException
    {
        List<Pair<String, String>> parts = new LinkedList<Pair<String, String>>();
        String[] entries = StringUtils.split(s, ENTRY_SEPARATOR);
        for (String entry: entries)
        {
            parts.add(splitPart(entry));
        }
        
        return parts;
    }

    private Pair<String, String> splitPart(String s) throws SqueezeException
    {
        int index = s.indexOf(KEY_VALUE_SEPARATOR);
        if (index < 0 || index > s.length() - 1)
        {
            throw new SqueezeException("Invalid entry '" + s + "' no key-value separator");
        }
        
        return new Pair<String, String>(decode(s.substring(0, index)), decode(s.substring(index + 1)));
    }

    private String decode(String s)
    {
        return s == null ? null : WebUtils.percentDecode(s);
    }

    private static class AllowedCharactersPredicate implements Predicate<Character>
    {
        public boolean satisfied(Character character)
        {
            return character != KEY_VALUE_SEPARATOR && character != ENTRY_SEPARATOR && character != '%';
        }
    }

    private static class NullSqueezer implements TypeSqueezer
    {
        public String squeeze(Object obj) throws SqueezeException
        {
            return null;
        }

        public Object unsqueeze(String s) throws SqueezeException
        {
            return null;
        }
    }
}
